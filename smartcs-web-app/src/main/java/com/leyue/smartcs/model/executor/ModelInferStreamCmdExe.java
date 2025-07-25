package com.leyue.smartcs.model.executor;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.common.constant.Constants;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ModelInferenceGateway;
import com.leyue.smartcs.dto.model.ModelInferStreamRequest;
import com.leyue.smartcs.dto.model.ModelInferResponse;
import com.leyue.smartcs.dto.sse.SSEMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * 模型流式推理命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelInferStreamCmdExe {

    private final ModelGateway modelGateway;
    private final ModelInferenceGateway modelInferenceGateway;
    private final RedissonClient redissonClient;

    /**
     * 执行模型流式推理
     */
    @SneakyThrows
    public void execute(ModelInferStreamRequest request, SseEmitter sse) throws IOException {
        long startTime = System.currentTimeMillis();
        final String sessionId = request.getSessionId() == null ? UUID.randomUUID().toString() : request.getSessionId();
        
        // 锁住当前会话的推理状态，避免并发推理
        RLock lock = redissonClient.getLock(Constants.MODEL_INFER_LOCK_PREFIX + sessionId);
        try {
            if (!lock.tryLock()) {
                sendErrorMessage(sse, sessionId, "推理繁忙，请稍后再试");
                return;
            }

            // 参数校验
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                throw new BizException("输入消息不能为空");
            }

            // 查询模型实例
            Optional<Model> modelOpt = modelGateway.findById(request.getModelId());
            if (!modelOpt.isPresent()) {
                throw new BizException("模型实例不存在");
            }

            Model model = modelOpt.get();
            
            // 检查模型状态
            if (!"ACTIVE".equals(model.getStatus())) {
                throw new BizException("模型实例未激活");
            }

            // 发送开始消息
            sendStartMessage(sse, sessionId);

            // 发送进度消息
            sendProgressMessage(sse, sessionId, "正在准备模型推理...");

            if (request.getEnableRAG() != null && request.getEnableRAG()) {
                sendProgressMessage(sse, sessionId, "正在检索相关知识...");
            }

            sendProgressMessage(sse, sessionId, "正在调用模型生成响应...");

            // 使用流式推理
            StringBuilder fullAnswer = new StringBuilder();
            modelInferenceGateway.inferStream(
                    model.getId(),
                    request.getMessage(),
                    sessionId,
                    request.getSystemPrompt(),
                    request.getEnableRAG(),
                    request.getKnowledgeId(),
                    request.getInferenceParams(),
                    request.getSaveToContext(),
                    (chunk) -> {
                        try {
                            fullAnswer.append(chunk);

                            // 发送流式数据
                            ModelInferResponse response = ModelInferResponse.builder()
                                    .content(chunk)
                                    .sessionId(sessionId)
                                    .status("STREAMING")
                                    .build();

                            sendDataMessage(sse, sessionId, response);
                        } catch (IOException e) {
                            log.error("发送流式数据失败: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
            );

            // 构建最终响应
            ModelInferResponse finalResponse = buildFinalResponse(sessionId, fullAnswer.toString(), startTime);

            // 发送完成消息
            sendCompleteMessage(sse, sessionId, finalResponse);

            log.info("模型流式推理执行完成: modelId={}, sessionId={}, 耗时={}ms", 
                    request.getModelId(), sessionId, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("模型流式推理执行失败: modelId={}, sessionId={}, error={}", 
                     request.getModelId(), sessionId, e.getMessage(), e);
            sendErrorMessage(sse, sessionId, "推理失败: " + e.getMessage());
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 构建最终响应
     */
    private ModelInferResponse buildFinalResponse(String sessionId, String content, long startTime) {
        return ModelInferResponse.builder()
                .content(content)
                .sessionId(sessionId)
                .status("COMPLETED")
                .inferenceTime(System.currentTimeMillis() - startTime)
                .fromCache(false)
                .build();
    }

    /**
     * 发送进度消息
     */
    private void sendProgressMessage(SseEmitter sse, String sessionId, String message) throws IOException {
        Long sessionIdLong = sessionId.hashCode() & 0x7fffffffL; // 简单转换为正数
        SSEMessage sseMessage = SSEMessage.progress(sessionIdLong, message);
        sse.send(SseEmitter.event()
                .name("progress")
                .id(sseMessage.getId())
                .data(JSON.toJSONString(sseMessage)));
    }

    /**
     * 发送数据消息
     */
    private void sendDataMessage(SseEmitter sse, String sessionId, Object data) throws IOException {
        Long sessionIdLong = sessionId.hashCode() & 0x7fffffffL;
        SSEMessage sseMessage = SSEMessage.data(sessionIdLong, data);
        sse.send(SseEmitter.event()
                .name("data")
                .id(sseMessage.getId())
                .data(JSON.toJSONString(sseMessage)));
    }

    /**
     * 发送完成消息
     */
    private void sendCompleteMessage(SseEmitter sse, String sessionId, Object finalData) throws IOException {
        Long sessionIdLong = sessionId.hashCode() & 0x7fffffffL;
        SSEMessage sseMessage = SSEMessage.complete(sessionIdLong, finalData);
        sse.send(SseEmitter.event()
                .name("complete")
                .id(sseMessage.getId())
                .data(JSON.toJSONString(sseMessage)));
        sse.complete();
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(SseEmitter sse, String sessionId, String error) throws IOException {
        Long sessionIdLong = sessionId.hashCode() & 0x7fffffffL;
        SSEMessage sseMessage = SSEMessage.error(sessionIdLong, error);
        sse.send(SseEmitter.event()
                .name("error")
                .id(sseMessage.getId())
                .data(JSON.toJSONString(sseMessage)));
        sse.complete();
    }

    /**
     * 发送开始消息
     */
    private void sendStartMessage(SseEmitter sse, String sessionId) throws IOException {
        Long sessionIdLong = sessionId.hashCode() & 0x7fffffffL;
        SSEMessage sseMessage = SSEMessage.start(sessionIdLong);
        sse.send(SseEmitter.event()
                .name("start")
                .id(sseMessage.getId())
                .data(JSON.toJSONString(sseMessage)));
    }
}