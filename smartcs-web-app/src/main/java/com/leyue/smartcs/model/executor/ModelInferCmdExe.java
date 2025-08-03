package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ModelInferenceGateway;
import com.leyue.smartcs.dto.model.ModelInferRequest;
import com.leyue.smartcs.dto.model.ModelInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 模型推理命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelInferCmdExe {

    private final ModelGateway modelGateway;
    private final ModelInferenceGateway modelInferenceGateway;

    /**
     * 执行模型推理
     *
     * @param request 推理请求
     * @return 推理响应
     */
    public SingleResponse<ModelInferResponse> execute(ModelInferRequest request) {
        try {
            // 参数校验
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                throw new BizException("输入消息不能为空");
            }

            // 查询模型实例
            Optional<Model> modelOpt = modelGateway.findById(request.getModelId());
            if (modelOpt.isEmpty()) {
                throw new BizException("模型实例不存在");
            }

            Model model = modelOpt.get();
            
            // 检查模型状态
            if (!"ACTIVE".equals(model.getStatus().name())) {
                throw new BizException("模型实例未激活");
            }

            // 生成或验证会话ID
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            long startTime = System.currentTimeMillis();

            // 执行推理
            String result = modelInferenceGateway.infer(
                    model.getId(),
                    request.getMessage(),
                    sessionId,
                    request.getSystemPrompt(),
                    request.getKnowledgeIds(),
                    request.getInferenceParams()
            );

            long inferenceTime = System.currentTimeMillis() - startTime;

            // 构建响应
            ModelInferResponse response = ModelInferResponse.builder()
                    .content(result)
                    .sessionId(sessionId)
                    .inferenceTime(inferenceTime)
                    .status("COMPLETED")
                    .fromCache(false)
                    .build();

            return SingleResponse.of(response);

        } catch (Exception e) {
            log.error("模型推理执行失败: modelId={}, error={}", request.getModelId(), e.getMessage(), e);
            
            // 构建错误响应
            ModelInferResponse errorResponse = ModelInferResponse.builder()
                    .sessionId(request.getSessionId())
                    .errorMessage(e.getMessage())
                    .status("FAILED")
                    .build();
            
            return SingleResponse.of(errorResponse);
        }
    }
}