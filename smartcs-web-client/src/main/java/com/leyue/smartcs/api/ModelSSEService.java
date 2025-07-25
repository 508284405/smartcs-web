package com.leyue.smartcs.api;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.leyue.smartcs.dto.model.ModelInferStreamRequest;

/**
 * 模型流式推理服务接口
 */
public interface ModelSSEService {
    
    /**
     * 模型流式推理
     * @param request 流式推理请求
     * @param sse SSE发射器
     */
    void inferStream(ModelInferStreamRequest request, SseEmitter sse);
}