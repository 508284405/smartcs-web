package com.leyue.smartcs.model.serviceimpl;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.api.ModelSSEService;
import com.leyue.smartcs.dto.model.ModelInferStreamRequest;
import com.leyue.smartcs.dto.sse.SSEMessage;
import com.leyue.smartcs.model.executor.ModelInferStreamCmdExe;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 模型流式推理服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelSSEServiceImpl implements ModelSSEService {

    private final ModelInferStreamCmdExe modelInferStreamCmdExe;
    private final Executor commonThreadPoolExecutor;

    @Override
    @SneakyThrows
    public void inferStream(ModelInferStreamRequest request, SseEmitter sse) {
        Long modelId = request.getModelId();
        String sessionId = request.getSessionId();

        // 异步处理流式推理请求
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        TracingSupport.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            try {
                // 执行流式推理命令
                modelInferStreamCmdExe.execute(request, sse);

            } catch (Exception e) {
                log.error("模型流式推理处理失败: modelId={}, sessionId={}, error={}", 
                         modelId, sessionId, e.getMessage(), e);
                try {
                    sse.send(SseEmitter.event()
                            .id("error_" + sessionId)
                            .name("error")
                            .data(JSON.toJSONString(SSEMessage.error(
                                    sessionId != null ? Long.valueOf(sessionId) : 0L, 
                                    e.getMessage()))));
                } catch (IOException ioException) {
                    log.error("发送错误消息失败: {}", ioException.getMessage());
                }
                sse.complete();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }, commonThreadPoolExecutor);
    }
}
