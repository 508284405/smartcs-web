package com.leyue.smartcs.chat.service;

import com.leyue.smartcs.chat.serviceimpl.MessageDistributionService;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.enums.MessageDeliveryStatus;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 消息可靠性保障服务
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageReliabilityService {
    
    private final MessageGateway messageGateway;
    private final StringRedisTemplate redisTemplate;
    private final MessageDistributionService messageDistributionService;
    
    private static final String DELIVERY_TRACKING_KEY = "message:delivery:tracking:";
    private static final String RETRY_QUEUE_KEY = "message:retry:queue";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long DELIVERY_TIMEOUT_MS = 30000; // 30秒投递超时
    
    /**
     * 发送消息并跟踪投递状态
     */
    @Transactional
    public void sendMessageWithReliability(Message message) {
        try {
            log.info("开始发送消息: {}", message.getMsgId());
            
            // 1. 更新消息状态为发送中
            updateDeliveryStatus(message.getMsgId(), MessageDeliveryStatus.SENDING);
            
            // 2. 设置投递超时检查
            scheduleDeliveryTimeoutCheck(message.getMsgId());
            
            // 3. 执行消息发送
            sendMessageInternal(message);
            
            // 4. 更新状态为已投递到服务器
            updateDeliveryStatus(message.getMsgId(), MessageDeliveryStatus.DELIVERED_TO_SERVER);
            
            log.info("消息发送成功: {}", message.getMsgId());
            
        } catch (Exception e) {
            log.error("消息发送失败: {}", message.getMsgId(), e);
            handleSendFailure(message, e);
        }
    }
    
    /**
     * 内部消息发送逻辑（带重试）
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void sendMessageInternal(Message message) {
        try {
            // 发送到消息队列
            messageDistributionService.distributeMessage(message);
            
        } catch (Exception e) {
            log.warn("消息发送重试: {} - {}", message.getMsgId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 处理发送失败
     */
    private void handleSendFailure(Message message, Exception error) {
        try {
            // 更新投递状态为失败
            updateDeliveryStatus(message.getMsgId(), MessageDeliveryStatus.FAILED);
            
            // 增加重试计数
            incrementRetryCount(message.getMsgId());
            
            // 检查是否可以重试
            int retryCount = getRetryCount(message.getMsgId());
            if (retryCount < MAX_RETRY_ATTEMPTS) {
                // 加入重试队列
                addToRetryQueue(message.getMsgId(), calculateRetryDelay(retryCount));
                updateDeliveryStatus(message.getMsgId(), MessageDeliveryStatus.RETRY_REQUIRED);
            } else {
                // 达到最大重试次数
                updateDeliveryStatus(message.getMsgId(), MessageDeliveryStatus.MAX_RETRY_REACHED);
                log.error("消息达到最大重试次数: {} - {}", message.getMsgId(), error.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理发送失败时出错: {}", message.getMsgId(), e);
        }
    }
    
    /**
     * 确认消息投递成功
     */
    public void confirmMessageDelivery(String msgId, String receiverId) {
        try {
            log.info("确认消息投递: {} -> {}", msgId, receiverId);
            
            updateDeliveryStatus(msgId, MessageDeliveryStatus.PUSHED_TO_RECEIVER);
            
            // 清理投递跟踪数据
            clearDeliveryTracking(msgId);
            
        } catch (Exception e) {
            log.error("确认消息投递失败: {}", msgId, e);
        }
    }
    
    /**
     * 确认消息被接收者确认
     */
    public void acknowledgeMessage(String msgId, String receiverId) {
        try {
            log.info("消息被确认: {} by {}", msgId, receiverId);
            
            updateDeliveryStatus(msgId, MessageDeliveryStatus.ACKNOWLEDGED);
            
            // 清理所有跟踪数据
            clearDeliveryTracking(msgId);
            clearRetryData(msgId);
            
        } catch (Exception e) {
            log.error("确认消息确认失败: {}", msgId, e);
        }
    }
    
    /**
     * 定时检查投递超时的消息
     */
    @Scheduled(fixedDelay = 10000) // 每10秒检查一次
    public void checkDeliveryTimeouts() {
        try {
            // 检查超时消息的逻辑
            List<String> timeoutMessages = findTimeoutMessages();
            
            for (String msgId : timeoutMessages) {
                log.warn("检测到投递超时消息: {}", msgId);
                handleDeliveryTimeout(msgId);
            }
            
        } catch (Exception e) {
            log.error("检查投递超时失败", e);
        }
    }
    
    /**
     * 定时处理重试队列
     */
    @Scheduled(fixedDelay = 5000) // 每5秒处理一次
    public void processRetryQueue() {
        try {
            List<String> retryMessages = getRetryMessages();
            
            for (String msgId : retryMessages) {
                processRetryMessage(msgId);
            }
            
        } catch (Exception e) {
            log.error("处理重试队列失败", e);
        }
    }
    
    /**
     * 处理重试消息
     */
    @Async
    public void processRetryMessage(String msgId) {
        try {
            Message message = messageGateway.findByMsgId(msgId);
            if (message != null) {
                log.info("重试发送消息: {}", msgId);
                sendMessageWithReliability(message);
            }
            
        } catch (Exception e) {
            log.error("重试消息失败: {}", msgId, e);
            handleSendFailure(messageGateway.findByMsgId(msgId), e);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    private void updateDeliveryStatus(String msgId, MessageDeliveryStatus status) {
        String key = DELIVERY_TRACKING_KEY + msgId;
        redisTemplate.opsForHash().put(key, "status", status.getCode().toString());
        redisTemplate.opsForHash().put(key, "updateTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 24小时过期
    }
    
    private void scheduleDeliveryTimeoutCheck(String msgId) {
        String key = DELIVERY_TRACKING_KEY + msgId;
        redisTemplate.opsForHash().put(key, "timeoutAt", 
                String.valueOf(System.currentTimeMillis() + DELIVERY_TIMEOUT_MS));
    }
    
    private void incrementRetryCount(String msgId) {
        String key = DELIVERY_TRACKING_KEY + msgId;
        redisTemplate.opsForHash().increment(key, "retryCount", 1);
    }
    
    private int getRetryCount(String msgId) {
        String key = DELIVERY_TRACKING_KEY + msgId;
        String count = (String) redisTemplate.opsForHash().get(key, "retryCount");
        return count != null ? Integer.parseInt(count) : 0;
    }
    
    private void addToRetryQueue(String msgId, long delayMs) {
        long retryTime = System.currentTimeMillis() + delayMs;
        redisTemplate.opsForZSet().add(RETRY_QUEUE_KEY, msgId, retryTime);
    }
    
    private long calculateRetryDelay(int retryCount) {
        // 指数退避：1秒, 2秒, 4秒
        return (long) Math.pow(2, retryCount) * 1000;
    }
    
    private void clearDeliveryTracking(String msgId) {
        redisTemplate.delete(DELIVERY_TRACKING_KEY + msgId);
    }
    
    private void clearRetryData(String msgId) {
        redisTemplate.opsForZSet().remove(RETRY_QUEUE_KEY, msgId);
    }
    
    private List<String> findTimeoutMessages() {
        // 实现超时消息查找逻辑
        // 这里可以扫描Redis中的跟踪数据，找出超时的消息
        return List.of(); // 简化实现
    }
    
    private List<String> getRetryMessages() {
        // 获取到达重试时间的消息
        long currentTime = System.currentTimeMillis();
        return redisTemplate.opsForZSet()
                .rangeByScore(RETRY_QUEUE_KEY, 0, currentTime)
                .stream()
                .toList();
    }
    
    private void handleDeliveryTimeout(String msgId) {
        updateDeliveryStatus(msgId, MessageDeliveryStatus.FAILED);
        // 可以触发重试逻辑
        Message message = messageGateway.findByMsgId(msgId);
        if (message != null) {
            handleSendFailure(message, new RuntimeException("Delivery timeout"));
        }
    }
}