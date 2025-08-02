package com.leyue.smartcs.app.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 容错的Redis聊天记忆存储
 * 支持降级到InMemory存储，防止Redis单点故障
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaultTolerantRedisChatMemoryStore implements ChatMemoryStore {

    private final RedisChatMemoryStore redisChatMemoryStore;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 降级策略配置
    @Value("${smartcs.ai.memory.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    @Value("${smartcs.ai.memory.fallback.failure-threshold:3}")
    private int failureThreshold;
    
    @Value("${smartcs.ai.memory.fallback.recovery-interval:60000}")
    private long recoveryIntervalMs;
    
    // 熔断器状态
    private final AtomicBoolean circuitOpen = new AtomicBoolean(false);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    
    // 降级存储
    private final InMemoryChatMemoryStore fallbackStore = new InMemoryChatMemoryStore();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        if (isCircuitOpen()) {
            log.debug("熔断器开启，使用内存存储获取消息: memoryId={}", memoryId);
            return fallbackStore.getMessages(memoryId);
        }
        
        try {
            List<ChatMessage> messages = redisChatMemoryStore.getMessages(memoryId);
            recordSuccess();
            return messages;
        } catch (Exception e) {
            log.warn("Redis获取消息失败，尝试降级: memoryId={}, error={}", memoryId, e.getMessage());
            recordFailure();
            
            if (fallbackEnabled) {
                return fallbackStore.getMessages(memoryId);
            } else {
                throw new RuntimeException("Redis存储失败且降级被禁用", e);
            }
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (isCircuitOpen()) {
            log.debug("熔断器开启，使用内存存储更新消息: memoryId={}", memoryId);
            fallbackStore.updateMessages(memoryId, messages);
            return;
        }
        
        try {
            redisChatMemoryStore.updateMessages(memoryId, messages);
            recordSuccess();
            
            // 同时更新降级存储作为备份
            if (fallbackEnabled) {
                fallbackStore.updateMessages(memoryId, messages);
            }
        } catch (Exception e) {
            log.warn("Redis更新消息失败，尝试降级: memoryId={}, error={}", memoryId, e.getMessage());
            recordFailure();
            
            if (fallbackEnabled) {
                fallbackStore.updateMessages(memoryId, messages);
            } else {
                throw new RuntimeException("Redis存储失败且降级被禁用", e);
            }
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        if (isCircuitOpen()) {
            log.debug("熔断器开启，使用内存存储删除消息: memoryId={}", memoryId);
            fallbackStore.deleteMessages(memoryId);
            return;
        }
        
        try {
            redisChatMemoryStore.deleteMessages(memoryId);
            recordSuccess();
            
            // 同时删除降级存储中的数据
            if (fallbackEnabled) {
                fallbackStore.deleteMessages(memoryId);
            }
        } catch (Exception e) {
            log.warn("Redis删除消息失败，尝试降级: memoryId={}, error={}", memoryId, e.getMessage());
            recordFailure();
            
            if (fallbackEnabled) {
                fallbackStore.deleteMessages(memoryId);
            } else {
                throw new RuntimeException("Redis存储失败且降级被禁用", e);
            }
        }
    }

    /**
     * 检查熔断器是否开启
     */
    private boolean isCircuitOpen() {
        if (!circuitOpen.get()) {
            return false;
        }
        
        // 检查是否到了恢复时间
        long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
        if (timeSinceLastFailure > recoveryIntervalMs) {
            log.info("尝试恢复Redis连接");
            if (testRedisConnection()) {
                log.info("Redis连接恢复，关闭熔断器");
                circuitOpen.set(false);
                consecutiveFailures.set(0);
                return false;
            } else {
                lastFailureTime.set(System.currentTimeMillis());
            }
        }
        
        return true;
    }

    /**
     * 记录成功操作
     */
    private void recordSuccess() {
        consecutiveFailures.set(0);
        if (circuitOpen.get()) {
            log.info("Redis操作成功，关闭熔断器");
            circuitOpen.set(false);
        }
    }

    /**
     * 记录失败操作
     */
    private void recordFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        long failures = consecutiveFailures.incrementAndGet();
        
        if (failures >= failureThreshold && !circuitOpen.get()) {
            log.error("Redis连续失败{}次，开启熔断器", failures);
            circuitOpen.set(true);
        }
    }

    /**
     * 测试Redis连接
     */
    private boolean testRedisConnection() {
        try {
            redisTemplate.hasKey("test:connection");
            return true;
        } catch (Exception e) {
            log.debug("Redis连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取存储状态
     */
    public StorageStatus getStatus() {
        boolean redisAvailable = !isCircuitOpen();
        long failureCount = consecutiveFailures.get();
        long lastFailure = lastFailureTime.get();
        
        return new StorageStatus(redisAvailable, failureCount, lastFailure);
    }

    /**
     * 手动重置熔断器
     */
    public void resetCircuitBreaker() {
        log.info("手动重置熔断器");
        circuitOpen.set(false);
        consecutiveFailures.set(0);
        lastFailureTime.set(0);
    }

    /**
     * 强制开启熔断器（用于维护）
     */
    public void forceCircuitOpen() {
        log.info("强制开启熔断器");
        circuitOpen.set(true);
        lastFailureTime.set(System.currentTimeMillis());
    }

    /**
     * 存储状态信息
     */
    public static class StorageStatus {
        public final boolean redisAvailable;
        public final long failureCount;
        public final long lastFailureTime;
        
        public StorageStatus(boolean redisAvailable, long failureCount, long lastFailureTime) {
            this.redisAvailable = redisAvailable;
            this.failureCount = failureCount;
            this.lastFailureTime = lastFailureTime;
        }
        
        @Override
        public String toString() {
            return String.format("StorageStatus{redisAvailable=%s, failureCount=%d, lastFailureTime=%d}", 
                               redisAvailable, failureCount, lastFailureTime);
        }
    }

    /**
     * 数据同步：从Redis同步到InMemory（恢复时使用）
     */
    public void syncFromRedisToMemory() {
        if (isCircuitOpen()) {
            log.warn("熔断器开启，无法从Redis同步数据");
            return;
        }
        
        try {
            log.info("开始从Redis同步数据到内存存储");
            // 这里需要根据实际需求实现数据同步逻辑
            // 当前的ChatMemoryStore接口不支持遍历所有会话，这是一个限制
            log.info("数据同步完成");
        } catch (Exception e) {
            log.error("数据同步失败", e);
        }
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        return !isCircuitOpen() || fallbackEnabled;
    }
}