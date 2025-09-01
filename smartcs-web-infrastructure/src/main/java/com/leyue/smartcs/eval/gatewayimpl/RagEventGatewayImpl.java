package com.leyue.smartcs.eval.gatewayimpl;

import com.leyue.smartcs.domain.eval.gateway.RagEventGateway;
import com.leyue.smartcs.dto.eval.event.RagEvent;
import com.leyue.smartcs.eval.producer.RagEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RAG事件网关实现
 * 委托给RagEventProducer处理具体的Kafka发送逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEventGatewayImpl implements RagEventGateway {
    
    private final RagEventProducer ragEventProducer;
    
//    @Override
//    public void sendAsync(RagEvent event) {
//        log.debug("通过网关异步发送RAG事件: eventId={}", event != null ? event.getEventId() : "null");
//        ragEventProducer.sendAsync(event);
//    }
    
//    @Override
//    public boolean sendSync(RagEvent event) {
//        log.debug("通过网关同步发送RAG事件: eventId={}", event != null ? event.getEventId() : "null");
//        return ragEventProducer.sendSync(event);
//    }
    
//    @Override
//    public void sendBatchAsync(List<RagEvent> events) {
//        log.debug("通过网关批量发送RAG事件: count={}", events != null ? events.size() : 0);
//        ragEventProducer.sendBatchAsync(events);
//    }
}