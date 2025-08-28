package com.leyue.smartcs.chat.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.api.OfflineMessageService;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息分发服务
 * 负责跨节点消息转发和本地消息投递
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDistributionService {

    private final WebSocketSessionManager sessionManager;
    private final OfflineMessageService offlineMessageService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发布私聊消息到Kafka
     * 
     * @param chatMessage 聊天消息
     * @param receiverId 接收者ID
     */
    public void publishDirectMessage(ChatMessage chatMessage, String receiverId) {
        try {
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            
            // 使用接收者ID作为分区键，确保同一用户的消息有序
            kafkaTemplate.send("im.direct", receiverId, messageJson)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("发布私聊消息到Kafka失败: receiverId={}, msgId={}", 
                                    receiverId, chatMessage.getMsgId(), throwable);
                        } else {
                            log.debug("私聊消息发布成功: receiverId={}, msgId={}, partition={}", 
                                    receiverId, chatMessage.getMsgId(), result.getRecordMetadata().partition());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("序列化私聊消息失败: receiverId={}, msgId={}", receiverId, chatMessage.getMsgId(), e);
        }
    }

    /**
     * 发布群聊消息到Kafka
     * 
     * @param chatMessage 群聊消息
     * @param memberIds 群成员ID列表
     */
    public void publishGroupMessage(ChatMessage chatMessage, List<Long> memberIds) {
        try {
            // 构建群聊消息载荷
            GroupMessagePayload payload = GroupMessagePayload.builder()
                    .chatMessage(chatMessage)
                    .memberIds(memberIds)
                    .build();
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            String groupKey = "group_" + chatMessage.getGroupId();
            
            // 使用群组ID作为分区键，确保同一群组的消息有序
            kafkaTemplate.send("im.group", groupKey, payloadJson)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("发布群聊消息到Kafka失败: groupId={}, msgId={}", 
                                    chatMessage.getGroupId(), chatMessage.getMsgId(), throwable);
                        } else {
                            log.debug("群聊消息发布成功: groupId={}, msgId={}, partition={}", 
                                    chatMessage.getGroupId(), chatMessage.getMsgId(), 
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("序列化群聊消息失败: groupId={}, msgId={}", 
                    chatMessage.getGroupId(), chatMessage.getMsgId(), e);
        }
    }

    /**
     * 发布系统事件到Kafka
     * 
     * @param event 事件对象
     */
    public void publishEvent(Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("im.event", eventJson);
        } catch (JsonProcessingException e) {
            log.error("发布系统事件失败", e);
        }
    }

    /**
     * 消费私聊消息进行跨节点分发
     */
    @KafkaListener(topics = "im.direct", groupId = "im-dispatcher", 
                   concurrency = "4", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleDirectMessage(@Payload String messageJson,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String receiverId,
                                   Acknowledgment acknowledgment) {
        log.debug("消费私聊消息: receiverId={}, partition={}", receiverId, partition);
        
        try {
            ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
            
            Long receiverIdLong = Long.valueOf(receiverId);
            
            // 检查接收者是否在当前节点在线
            if (sessionManager.isUserOnline(receiverIdLong)) {
                // 用户在线，推送消息
                sessionManager.sendToUser(receiverId, "messages", chatMessage);
                log.info("跨节点私聊消息推送成功: receiverId={}, msgId={}", receiverId, chatMessage.getMsgId());
            } else {
                // 用户离线，保存离线消息
                String conversationId = String.valueOf(chatMessage.getSessionId());
                String msgBrief = generateMessageBrief(chatMessage.getContent());
                offlineMessageService.saveOfflineMessage(receiverIdLong, conversationId, 
                        chatMessage.getMsgId(), msgBrief);
                log.info("用户离线，保存离线消息: receiverId={}, msgId={}", receiverId, chatMessage.getMsgId());
            }
            
            // 手动确认消息处理完成
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("处理跨节点私聊消息失败: receiverId={}, partition={}", receiverId, partition, e);
            // 不确认消息，让Kafka重试
        }
    }

    /**
     * 消费群聊消息进行跨节点分发
     */
    @KafkaListener(topics = "im.group", groupId = "im-dispatcher", 
                   concurrency = "8", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleGroupMessage(@Payload String payloadJson,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String groupKey,
                                  Acknowledgment acknowledgment) {
        log.debug("消费群聊消息: groupKey={}, partition={}", groupKey, partition);
        
        try {
            GroupMessagePayload payload = objectMapper.readValue(payloadJson, GroupMessagePayload.class);
            ChatMessage chatMessage = payload.getChatMessage();
            List<Long> memberIds = payload.getMemberIds();
            
            String conversationId = "group_" + chatMessage.getGroupId();
            String msgBrief = generateMessageBrief(chatMessage.getContent());
            
            int onlineCount = 0;
            int offlineCount = 0;
            
            // 分别处理在线和离线成员
            for (Long memberId : memberIds) {
                // 跳过发送者
                if (memberId.equals(Long.valueOf(chatMessage.getFromUserId()))) {
                    continue;
                }
                
                if (sessionManager.isUserOnline(memberId)) {
                    // 在线成员，推送消息
                    sessionManager.sendToUser(String.valueOf(memberId), "messages", chatMessage);
                    onlineCount++;
                } else {
                    // 离线成员，保存离线消息
                    offlineMessageService.saveOfflineMessage(memberId, conversationId, 
                            chatMessage.getMsgId(), msgBrief);
                    offlineCount++;
                }
            }
            
            log.info("跨节点群聊消息分发完成: groupId={}, msgId={}, 在线={}, 离线={}", 
                    chatMessage.getGroupId(), chatMessage.getMsgId(), onlineCount, offlineCount);
            
            // 手动确认消息处理完成
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("处理跨节点群聊消息失败: groupKey={}, partition={}", groupKey, partition, e);
            // 不确认消息，让Kafka重试
        }
    }

    /**
     * 消费系统事件进行审计和统计
     */
    @KafkaListener(topics = "im.event", groupId = "im-audit", 
                   concurrency = "2", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleSystemEvent(@Payload String eventJson,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 Acknowledgment acknowledgment) {
        log.debug("消费系统事件: partition={}", partition);
        
        try {
            // 这里可以处理审计、统计、机器人联动等逻辑
            // 当前简化处理，只记录日志
            log.info("系统事件处理: {}", eventJson);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("处理系统事件失败: partition={}", partition, e);
        }
    }

    /**
     * 生成消息摘要
     */
    private String generateMessageBrief(String content) {
        if (content == null || content.isEmpty()) {
            return "[空消息]";
        }
        
        int maxLength = 50;
        if (content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 群聊消息载荷
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GroupMessagePayload {
        private ChatMessage chatMessage;
        private List<Long> memberIds;
    }
}