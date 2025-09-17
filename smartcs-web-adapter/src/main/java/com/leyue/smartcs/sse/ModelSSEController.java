package com.leyue.smartcs.sse;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.api.ModelSSEService;
import com.leyue.smartcs.dto.model.ModelInferStreamRequest;
import com.leyue.smartcs.dto.sse.SSEMessage;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 模型SSE流式推理控制器
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class ModelSSEController {

    private final ModelSSEService modelSSEService;

    @PostMapping(value = "/model/{modelId}/infer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SentinelResource(value = "model:infer:controller",
            blockHandler = "modelInferStreamBlockHandler",
            fallback = "modelInferStreamFallback")
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

    public SseEmitter modelInferStreamFallback(Long modelId, ModelInferStreamRequest request, Throwable throwable) {
        log.warn("模型推理接口降级: modelId={}, error={}", modelId, throwable.getMessage());
        SseEmitter emitter = new SseEmitter(Duration.ofSeconds(30).toMillis());
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(JSON.toJSONString(SSEMessage.error(modelId, "模型服务繁忙，请稍后重试"))));
        } catch (IOException e) {
            log.error("发送降级消息失败", e);
        } finally {
            emitter.complete();
        }
        return emitter;
    }

    public SseEmitter modelInferStreamBlockHandler(Long modelId, ModelInferStreamRequest request, BlockException ex) {
        log.warn("模型推理接口触发限流: modelId={}, rule={}", modelId, ex.getRule());
        return modelInferStreamFallback(modelId, request, ex);
    }
}
