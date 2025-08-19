package com.leyue.smartcs.sse;

import java.io.IOException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.api.ModelSSEService;
import com.leyue.smartcs.dto.model.ModelInferStreamRequest;
import com.leyue.smartcs.dto.sse.SSEMessage;

import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型SSE流式推理控制器
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class ModelSSEController {

    private final ModelSSEService modelSSEService;

    // 移除RouterFunction相关代码，改为标准Spring MVC接口
    @PostMapping(value = "/model/{modelId}/infer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter modelInferStream(@PathVariable Long modelId, @RequestBody ModelInferStreamRequest request) {
        request.setModelId(modelId);
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(10).toMillis());
        // 可选：设置回调（如有需要）
        emitter.onCompletion(() -> log.info("模型推理SSE连接完成: modelId={}, sessionId={}", modelId, request.getSessionId()));
        emitter.onTimeout(() -> {
            log.warn("模型推理SSE连接超时: modelId={}, sessionId={}", modelId, request.getSessionId());
            try {
                emitter.send(SseEmitter.event()
                        .id("timeout_" + request.getSessionId())
                        .name("timeout")
                        .data(JSON.toJSONString(SSEMessage.error(modelId, "连接超时"))));
            } catch (IOException e) {
                log.error("发送超时消息失败: {}", e.getMessage());
            }
            emitter.complete();
        });
        emitter.onError((throwable) -> {
            log.error("模型推理SSE连接错误: modelId={}, sessionId={}, error={}", 
                     modelId, request.getSessionId(), throwable.getMessage());
            try {
                emitter.send(SseEmitter.event()
                        .id("error_" + request.getSessionId())
                        .name("error")
                        .data(JSON.toJSONString(
                                SSEMessage.error(modelId, throwable.getMessage()))));
            } catch (IOException e) {
                log.error("发送错误消息失败: {}", e.getMessage());
            }
            emitter.complete();
        });
        modelSSEService.inferStream(request, emitter);
        return emitter;
    }
}