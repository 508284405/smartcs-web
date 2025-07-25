package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ModelTaskGateway;
import com.leyue.smartcs.dto.model.ModelInferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * 模型异步推理命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelInferAsyncCmdExe {

    private final ModelGateway modelGateway;
    private final ModelTaskGateway modelTaskGateway;

    /**
     * 执行异步模型推理
     *
     * @param request 推理请求
     * @return 任务ID
     */
    public SingleResponse<String> execute(ModelInferRequest request) {
        try {
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

            // 生成任务ID
            String taskId = UUID.randomUUID().toString();

            // 创建推理任务
            String result = modelTaskGateway.createInferenceTask(
                    taskId,
                    model.getId(),
                    request.getMessage(),
                    request.getSessionId(),
                    request.getSystemPrompt(),
                    request.getEnableRAG(),
                    request.getKnowledgeId(),
                    request.getInferenceParams(),
                    request.getSaveToContext()
            );

            return SingleResponse.of(taskId);

        } catch (Exception e) {
            log.error("异步模型推理创建失败: modelId={}, error={}", request.getModelId(), e.getMessage(), e);
            throw new BizException("异步推理任务创建失败: " + e.getMessage());
        }
    }
}