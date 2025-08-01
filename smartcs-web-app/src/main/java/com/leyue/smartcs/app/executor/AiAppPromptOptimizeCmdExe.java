package com.leyue.smartcs.app.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeCmd;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeResponse;
import com.leyue.smartcs.dto.model.ModelInferRequest;
import com.leyue.smartcs.dto.model.ModelInferResponse;
import com.leyue.smartcs.api.ModelService;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI应用Prompt优化命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppPromptOptimizeCmdExe {
    
    private final ModelService modelService;
    private final ModelGateway modelGateway;
    
    private static final String OPTIMIZE_PROMPT_TEMPLATE = """
            你是一个专业的AI Prompt优化专家。请帮助优化下面的prompt，使其更加清晰、具体和有效。
            
            原始Prompt：
            {originalPrompt}
            
            优化目标：
            {optimizeGoal}
            
            请从以下方面优化Prompt：
            1. 明确角色定位和任务描述
            2. 增加具体的指令和约束条件
            3. 提供清晰的输出格式要求
            4. 添加必要的上下文信息
            5. 确保逻辑清晰、表达准确
            
            请返回JSON格式的回复，包含以下字段：
            {
                "optimized_prompt": "优化后的prompt内容",
                "explanation": "优化说明，详细解释做了哪些改进"
            }
            """;
    
    public SingleResponse<AiAppPromptOptimizeResponse> execute(AiAppPromptOptimizeCmd cmd) {
        try {
            log.info("开始执行Prompt优化: appId={}", cmd.getAppId());
            
            // 使用指定的模型或默认模型
            Long modelId = cmd.getModelId();
            if (modelId == null) {
                // 获取默认的文本生成模型
                modelId = getDefaultTextModel();
            }
            
            // 构建优化prompt
            String optimizePrompt = buildOptimizePrompt(cmd);
            
            // 调用模型进行推理
            ModelInferRequest inferRequest = new ModelInferRequest();
            inferRequest.setModelId(modelId);
            inferRequest.setMessage(optimizePrompt);
            inferRequest.setInferenceParams("{\"max_tokens\":2000,\"temperature\":0.7}");
            
            SingleResponse<ModelInferResponse> inferResponse = modelService.infer(inferRequest);
            
            if (!inferResponse.isSuccess()) {
                log.error("模型推理失败: {}", inferResponse.getErrMessage());
                return SingleResponse.buildFailure("OPTIMIZE_FAILED", "Prompt优化失败: " + inferResponse.getErrMessage());
            }
            
            // 解析响应并构建结果
            String responseText = inferResponse.getData().getContent();
            AiAppPromptOptimizeResponse response = parseOptimizeResponse(responseText, cmd, modelId);
            
            log.info("Prompt优化完成: appId={}, taskId={}", cmd.getAppId(), response.getTaskId());
            return SingleResponse.of(response);
            
        } catch (Exception e) {
            log.error("Prompt优化执行异常: appId={}", cmd.getAppId(), e);
            return SingleResponse.buildFailure("OPTIMIZE_ERROR", "Prompt优化执行异常: " + e.getMessage());
        }
    }
    
    private String buildOptimizePrompt(AiAppPromptOptimizeCmd cmd) {
        return OPTIMIZE_PROMPT_TEMPLATE
                .replace("{originalPrompt}", cmd.getOriginalPrompt())
                .replace("{optimizeGoal}", cmd.getOptimizeGoal() != null ? cmd.getOptimizeGoal() : "通用优化");
    }
    
    private AiAppPromptOptimizeResponse parseOptimizeResponse(String responseText, 
                                                            AiAppPromptOptimizeCmd cmd, 
                                                            Long modelId) {
        try {
            // 尝试解析JSON响应
            if (responseText.contains("optimized_prompt") && responseText.contains("explanation")) {
                // 简单的JSON解析，实际项目中可以使用Jackson等JSON库
                String optimizedPrompt = extractJsonField(responseText, "optimized_prompt");
                String explanation = extractJsonField(responseText, "explanation");
                
                return AiAppPromptOptimizeResponse.builder()
                        .originalPrompt(cmd.getOriginalPrompt())
                        .optimizedPrompt(optimizedPrompt)
                        .optimizeExplanation(explanation)
                        .modelName(getModelName(modelId))
                        .taskId(generateTaskId())
                        .build();
            } else {
                // 如果不是JSON格式，直接使用响应文本作为优化结果
                return AiAppPromptOptimizeResponse.builder()
                        .originalPrompt(cmd.getOriginalPrompt())
                        .optimizedPrompt(responseText)
                        .optimizeExplanation("AI模型已对原始prompt进行了优化")
                        .modelName(getModelName(modelId))
                        .taskId(generateTaskId())
                        .build();
            }
        } catch (Exception e) {
            log.warn("解析优化响应失败，使用原始响应: {}", e.getMessage());
            return AiAppPromptOptimizeResponse.builder()
                    .originalPrompt(cmd.getOriginalPrompt())
                    .optimizedPrompt(responseText)
                    .optimizeExplanation("AI模型已对原始prompt进行了优化")
                    .modelName(getModelName(modelId))
                    .taskId(generateTaskId())
                    .build();
        }
    }
    
    private String extractJsonField(String json, String fieldName) {
        // 简单的JSON字段提取，实际项目中应使用专业的JSON解析库
        String searchPattern = "\"" + fieldName + "\"\\s*:\\s*\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) {
            return "";
        }
        
        startIndex = startIndex + searchPattern.length() - 1; // 回退到引号位置
        int endIndex = json.indexOf("\"", startIndex + 1);
        while (endIndex > 0 && json.charAt(endIndex - 1) == '\\') {
            endIndex = json.indexOf("\"", endIndex + 1);
        }
        
        if (endIndex == -1) {
            return "";
        }
        
        return json.substring(startIndex + 1, endIndex);
    }
    
    private Long getDefaultTextModel() {
        // 获取默认的文本生成模型ID，这里先返回固定值
        // 实际实现中应该查询数据库获取默认模型
        return 1L;
    }
    
    private String getModelName(Long modelId) {
        try {
            // 根据模型ID获取模型名称
            return modelGateway.findById(modelId)
                    .map(model -> model.getLabel())
                    .orElse("Unknown Model");
        } catch (Exception e) {
            log.warn("获取模型名称失败: modelId={}", modelId, e);
            return "Unknown Model";
        }
    }
    
    private String generateTaskId() {
        return "optimize_" + System.currentTimeMillis();
    }
}