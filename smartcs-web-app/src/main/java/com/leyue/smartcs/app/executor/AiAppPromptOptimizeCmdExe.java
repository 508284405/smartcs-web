package com.leyue.smartcs.app.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeCmd;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeResponse;
import com.leyue.smartcs.model.ai.DynamicModelManager;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI应用Prompt优化命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppPromptOptimizeCmdExe {
    
    private final DynamicModelManager dynamicModelManager;
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
            
            要求：只输出标准JSON，不要添加代码块标记、注释或解释。
            """;
    
    public SingleResponse<AiAppPromptOptimizeResponse> execute(AiAppPromptOptimizeCmd cmd) {
        try {
            log.info("开始执行Prompt优化: appId={}", cmd.getAppId());
            
            // 使用指定的模型或默认模型
            Long modelId = cmd.getModelId();
            
            // 构建优化prompt
            String optimizePrompt = buildOptimizePrompt(cmd);
            
            ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
            UserMessage userMessage = UserMessage.from(optimizePrompt);
            ChatResponse llmResponse = chatModel.chat(userMessage);
            
            if (llmResponse == null || llmResponse.aiMessage() == null) {
                log.error("LLM响应为空: modelId={}", modelId);
                return SingleResponse.buildFailure("OPTIMIZE_FAILED", "LLM响应为空");
            }
            
            // 解析响应并构建结果
            String responseText = llmResponse.aiMessage().text();
            AiAppPromptOptimizeResponse response = parseOptimizeResponse(responseText, cmd, modelId);
            
            log.info("Prompt优化完成: appId={}", cmd.getAppId());
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
            // 检查响应内容是否为空
            if (responseText == null || responseText.trim().isEmpty()) {
                throw new RuntimeException("模型返回内容为空");
            }
            
            String cleanedResponse = responseText.trim();
            log.info("模型响应内容: {}", cleanedResponse);
            
            // 尝试使用JSON库解析响应
            try {
                JSONObject jsonResponse = JSON.parseObject(cleanedResponse);
                
                String optimizedPrompt = jsonResponse.getString("optimized_prompt");
                String explanation = jsonResponse.getString("explanation");
                
                // 验证提取的内容
                if (optimizedPrompt != null && !optimizedPrompt.trim().isEmpty()) {
                    log.info("成功解析JSON响应: optimizedPrompt长度={}, explanation长度={}", 
                            optimizedPrompt.length(), explanation != null ? explanation.length() : 0);
                    
                    return AiAppPromptOptimizeResponse.builder()
                            .originalPrompt(cmd.getOriginalPrompt())
                            .optimizedPrompt(optimizedPrompt.trim())
                            .optimizeExplanation(explanation != null && !explanation.trim().isEmpty() ? 
                                    explanation.trim() : "AI模型已对原始prompt进行了优化")
                            .modelName(getModelName(modelId))
                            .taskId(null) // 同步调用不需要taskId
                            .build();
                } else {
                    log.warn("JSON解析成功但optimized_prompt字段为空");
                }
            } catch (Exception jsonException) {
                log.warn("JSON解析失败，尝试手动提取: {}", jsonException.getMessage());
                
                // 如果JSON库解析失败，尝试手动提取
                if (cleanedResponse.contains("optimized_prompt") && cleanedResponse.contains("explanation")) {
                    String optimizedPrompt = extractJsonField(cleanedResponse, "optimized_prompt");
                    String explanation = extractJsonField(cleanedResponse, "explanation");
                    
                    if (!optimizedPrompt.isEmpty()) {
                        log.info("手动提取成功: optimizedPrompt长度={}, explanation长度={}", 
                                optimizedPrompt.length(), explanation.length());
                        
                        return AiAppPromptOptimizeResponse.builder()
                                .originalPrompt(cmd.getOriginalPrompt())
                                .optimizedPrompt(optimizedPrompt)
                                .optimizeExplanation(explanation.isEmpty() ? "AI模型已对原始prompt进行了优化" : explanation)
                                .modelName(getModelName(modelId))
                                .taskId(null) // 同步调用不需要taskId
                                .build();
                    }
                }
            }
            
            // 如果所有解析方法都失败，直接使用响应文本作为优化结果
            log.warn("无法解析JSON响应，直接使用响应文本作为优化结果");
            return AiAppPromptOptimizeResponse.builder()
                    .originalPrompt(cmd.getOriginalPrompt())
                    .optimizedPrompt(cleanedResponse)
                    .optimizeExplanation("AI模型已对原始prompt进行了优化，但响应格式需要手动处理")
                    .modelName(getModelName(modelId))
                    .taskId(null) // 同步调用不需要taskId
                    .build();
                    
        } catch (Exception e) {
            log.error("解析优化响应失败: {}", e.getMessage(), e);
            return AiAppPromptOptimizeResponse.builder()
                    .originalPrompt(cmd.getOriginalPrompt())
                    .optimizedPrompt(responseText != null ? responseText : "优化失败，请重试")
                    .optimizeExplanation("解析优化结果时出现异常: " + e.getMessage())
                    .modelName(getModelName(modelId))
                    .taskId(null) // 同步调用不需要taskId
                    .build();
        }
    }
    
    private String extractJsonField(String json, String fieldName) {
        // 简单的JSON字段提取，处理转义字符和多行文本
        String searchPattern = "\"" + fieldName + "\"\\s*:\\s*\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) {
            return "";
        }
        
        startIndex = startIndex + searchPattern.length() - 1; // 回退到引号位置
        StringBuilder result = new StringBuilder();
        boolean inEscape = false;
        
        for (int i = startIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (inEscape) {
                // 处理转义字符
                switch (c) {
                    case 'n': result.append('\n'); break;
                    case 't': result.append('\t'); break;
                    case 'r': result.append('\r'); break;
                    case '\\': result.append('\\'); break;
                    case '"': result.append('"'); break;
                    default: result.append(c); break;
                }
                inEscape = false;
            } else if (c == '\\') {
                inEscape = true;
            } else if (c == '"') {
                // 结束引号
                break;
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
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
    
}