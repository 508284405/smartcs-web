package com.leyue.smartcs.eval.producer;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.dto.eval.event.RagEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * RAG事件Kafka生产者
 * 负责将RAG评估事件发送到Kafka消息队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "eval.enabled", havingValue = "true", matchIfMissing = true)
public class RagEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    // Kafka主题名称，从配置中获取，默认为rag.events
    @Value("${eval.kafka.topics.rag-events:rag.events}")
    private String topicRagEvents;
    
    /**
     * 异步发送RAG事件
     * 
     * @param event RAG事件对象
     */
    public void sendAsync(RagEvent event) {
        if (event == null) {
            log.warn("RAG事件为空，跳过发送");
            return;
        }
        
        try {
            // 序列化事件为JSON
            String eventJson = JSON.toJSONString(event);
            
            // 使用事件ID作为消息key，确保同一事件的消息有序
            String messageKey = event.getEventId();
            
            // 异步发送到Kafka
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicRagEvents, messageKey, eventJson);
            
            // 添加回调处理
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送RAG事件到Kafka失败: eventId={}, topic={}, error={}", 
                            event.getEventId(), topicRagEvents, ex.getMessage(), ex);
                } else {
                    log.debug("成功发送RAG事件到Kafka: eventId={}, topic={}, partition={}, offset={}", 
                            event.getEventId(), topicRagEvents, 
                            result.getRecordMetadata().partition(), 
                            result.getRecordMetadata().offset());
                }
            });
            
        } catch (Exception e) {
            log.error("发送RAG事件到Kafka失败(构建阶段): eventId={}, topic={}, error={}",
                    event != null ? event.getEventId() : "null", topicRagEvents, e.getMessage(), e);
        }
    }
    
    /**
     * 同步发送RAG事件（用于重要场景）
     * 
     * @param event RAG事件对象
     * @return 是否发送成功
     */
    public boolean sendSync(RagEvent event) {
        if (event == null) {
            log.warn("RAG事件为空，跳过发送");
            return false;
        }
        
        try {
            // 序列化事件为JSON
            String eventJson = JSON.toJSONString(event);
            String messageKey = event.getEventId();
            
            // 同步发送到Kafka
            SendResult<String, String> result = kafkaTemplate.send(topicRagEvents, messageKey, eventJson).get();
            
            log.debug("同步发送RAG事件成功: eventId={}, partition={}, offset={}", 
                    event.getEventId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            
            return true;
            
        } catch (Exception e) {
            log.error("同步发送RAG事件失败: eventId={}, error={}", event.getEventId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 批量异步发送RAG事件
     * 
     * @param events RAG事件列表
     */
    public void sendBatchAsync(java.util.List<RagEvent> events) {
        if (events == null || events.isEmpty()) {
            log.debug("RAG事件列表为空，跳过批量发送");
            return;
        }
        
        log.debug("开始批量发送RAG事件: count={}", events.size());
        
        for (RagEvent event : events) {
            sendAsync(event);
        }
        
        log.debug("批量发送RAG事件完成: count={}", events.size());
    }
}
