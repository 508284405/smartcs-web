package com.leyue.smartcs.eval.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.dto.eval.event.RagEvent;
import com.leyue.smartcs.eval.config.RagKafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * RAG评估事件生产者
 * 负责异步发送评估事件到Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEventProducer {
    
    private final KafkaTemplate<String, String> ragEventKafkaTemplate;
    private final RagKafkaConfig ragKafkaConfig;
    private final ObjectMapper objectMapper;
    
    /**
     * 异步发送RAG评估事件
     * 
     * @param event RAG事件
     */
    public void sendAsync(RagEvent event) {
        if (event == null) {
            log.warn("尝试发送空的RAG事件，已忽略");
            return;
        }
        
        try {
            // 序列化事件为JSON
            String eventJson = objectMapper.writeValueAsString(event);
            String key = determineKey(event);
            String topic = ragKafkaConfig.getRagEventsTopic();
            
            // 异步发送
            CompletableFuture<SendResult<String, String>> future = 
                ragEventKafkaTemplate.send(topic, key, eventJson);
            
            // 设置成功回调
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    handleSendFailure(event, key, topic, ex);
                } else {
                    handleSendSuccess(event, key, result);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("RAG事件JSON序列化失败: eventId={}, error={}", 
                    event.getEventId(), e.getMessage(), e);
            // 发送到死信队列
            sendToDlq(event, "序列化失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("发送RAG事件时发生异常: eventId={}, error={}", 
                    event.getEventId(), e.getMessage(), e);
        }
    }
    
    /**
     * 确定消息的分区键
     * 优先使用traceId，其次使用eventId
     */
    private String determineKey(RagEvent event) {
        if (event.getTraceId() != null && !event.getTraceId().isEmpty()) {
            return event.getTraceId();
        }
        return event.getEventId();
    }
    
    /**
     * 处理发送成功
     */
    private void handleSendSuccess(RagEvent event, String key, SendResult<String, String> result) {
        if (log.isDebugEnabled()) {
            log.debug("RAG事件发送成功: eventId={}, key={}, partition={}, offset={}", 
                    event.getEventId(), key, 
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        }
    }
    
    /**
     * 处理发送失败
     * 失败的事件会被发送到死信队列
     */
    private void handleSendFailure(RagEvent event, String key, String topic, Throwable ex) {
        log.error("RAG事件发送失败: eventId={}, key={}, topic={}, error={}", 
                event.getEventId(), key, topic, ex.getMessage(), ex);
        
        // 发送到死信队列
        sendToDlq(event, "发送失败: " + ex.getMessage());
    }
    
    /**
     * 发送事件到死信队列
     */
    private void sendToDlq(RagEvent event, String reason) {
        try {
            String dlqTopic = ragKafkaConfig.getRagEventsDlqTopic();
            String eventJson = objectMapper.writeValueAsString(event);
            String key = determineKey(event);
            
            // 添加失败原因到JSON中
            String dlqJson = addFailureReason(eventJson, reason);
            
            // 同步发送到DLQ，确保不丢失
            ragEventKafkaTemplate.send(dlqTopic, key, dlqJson).get();
            
            log.warn("RAG事件已发送到死信队列: eventId={}, reason={}, dlqTopic={}", 
                    event.getEventId(), reason, dlqTopic);
            
        } catch (Exception e) {
            log.error("发送RAG事件到死信队列失败: eventId={}, reason={}, error={}", 
                    event.getEventId(), reason, e.getMessage(), e);
        }
    }
    
    /**
     * 在JSON中添加失败原因
     */
    private String addFailureReason(String originalJson, String reason) {
        try {
            // 简单的JSON添加失败原因字段
            if (originalJson.endsWith("}")) {
                int insertPos = originalJson.lastIndexOf("}");
                return originalJson.substring(0, insertPos) + 
                       ",\"dlqReason\":\"" + escapeJson(reason) + "\"," +
                       "\"dlqTimestamp\":" + System.currentTimeMillis() + 
                       originalJson.substring(insertPos);
            }
        } catch (Exception e) {
            log.debug("添加DLQ信息失败，使用原始JSON", e);
        }
        return originalJson;
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}