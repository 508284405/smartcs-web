package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.event.RagEvent;

import java.util.List;

/**
 * RAG事件网关接口
 * 负责RAG评估事件的发送
 */
public interface RagEventGateway {
    
    /**
     * 异步发送RAG事件
     * 
     * @param event RAG事件对象
     */
    void sendAsync(RagEvent event);
    
    /**
     * 同步发送RAG事件
     * 
     * @param event RAG事件对象
     * @return 是否发送成功
     */
    boolean sendSync(RagEvent event);
    
    /**
     * 批量异步发送RAG事件
     * 
     * @param events RAG事件列表
     */
    void sendBatchAsync(List<RagEvent> events);
}