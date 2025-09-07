package com.leyue.smartcs.moderation.service;

import com.leyue.smartcs.domain.moderation.ModerationRecord;
import com.leyue.smartcs.domain.moderation.enums.ModerationResult;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.moderation.service.ModerationPromptGenerator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 基于LangChain4j的AI内容审核服务
 * 使用LLM进行智能内容审核和风险评估
 * 支持根据modelId动态选择AI模型
 */
@Slf4j
@Service
public class LangChain4jModerationService {

    private final DynamicModelManager dynamicModelManager;
    private final ModerationPromptGenerator promptGenerator;

    @Value("${moderation.ai.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${moderation.ai.enabled:true}")
    private boolean aiModerationEnabled;

    @Value("${moderation.ai.model-name:gpt-3.5-turbo}")
    private String defaultModelName;

    @Value("${moderation.ai.default-scenario:USER_CHAT}")
    private String defaultScenario;

    public LangChain4jModerationService(DynamicModelManager dynamicModelManager, ModerationPromptGenerator promptGenerator) {
        this.dynamicModelManager = dynamicModelManager;
        this.promptGenerator = promptGenerator;
    }

    /**
     * AI审核助手接口 - 支持动态prompt
     */
    interface ModerationAssistant {
        /**
         * 使用动态prompt进行内容审核
         */
        String moderateWithPrompt(String prompt);
        
        /**
         * 快速安全检查（保留向后兼容性）
         */
        @UserMessage("""
            请对以下内容进行快速安全检查，判断是否需要进一步审核。

            内容：{{content}}

            请只返回：
            - "SAFE" - 内容安全，可以通过
            - "UNSAFE" - 内容可能存在风险，需要详细审核
            - "BLOCKED" - 内容明显违规，直接阻断

            只返回一个词，不要额外说明。
            """)
        String quickModerate(String content);
    }

    /**
     * 执行AI内容审核（使用指定模型和场景）
     */
    public CompletableFuture<AiModerationResult> moderateContent(String content, Long modelId, String scenario) {
        if (!aiModerationEnabled) {
            return CompletableFuture.completedFuture(
                    AiModerationResult.disabled("AI moderation is disabled")
            );
        }

        if (content == null || content.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    AiModerationResult.approved("Empty content", 1.0)
            );
        }

        if (modelId == null) {
            return CompletableFuture.completedFuture(
                    AiModerationResult.error("Model ID is required")
            );
        }

        return TracingSupport.supplyAsync(() -> {
            try {
                // 1. 生成动态prompt
                String targetScenario = scenario != null ? scenario : defaultScenario;
                ModerationPromptGenerator.GeneratedPrompt generatedPrompt = 
                    promptGenerator.generatePromptForScenario(targetScenario, content);
                
                if (!generatedPrompt.isSuccess()) {
                    log.error("Failed to generate prompt: {}", generatedPrompt.getErrorMessage());
                    return AiModerationResult.error("Failed to generate prompt: " + generatedPrompt.getErrorMessage());
                }

                // 2. 动态获取ChatModel
                ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
                
                // 3. 动态创建ModerationAssistant
                ModerationAssistant moderationAssistant = AiServices.builder(ModerationAssistant.class)
                        .chatModel(chatModel)
                        .build();

                // 4. 执行审核
                long startTime = System.currentTimeMillis();
                String result = moderationAssistant.moderateWithPrompt(generatedPrompt.getPrompt());
                long processingTime = System.currentTimeMillis() - startTime;

                // 5. 解析结果
                AiModerationResult moderationResult = parseAiResult(result, processingTime, modelId, generatedPrompt);
                log.debug("AI moderation completed in {}ms, modelId: {}, scenario: {}, result: {}", 
                         processingTime, modelId, targetScenario, moderationResult.getResult());
                return moderationResult;

            } catch (Exception e) {
                log.error("AI moderation failed for content length: {}, modelId: {}, scenario: {}", 
                         content.length(), modelId, scenario, e);
                return AiModerationResult.error("AI moderation failed: " + e.getMessage());
            }
        }).completeOnTimeout(
                AiModerationResult.timeout("AI moderation timeout"), 
                timeoutSeconds, 
                TimeUnit.SECONDS
        );
    }

    /**
     * 执行AI内容审核（使用指定模型，默认场景）
     */
    public CompletableFuture<AiModerationResult> moderateContent(String content, Long modelId) {
        return moderateContent(content, modelId, null);
    }

    /**
     * 执行快速AI预检（使用指定模型和语言）
     */
    public CompletableFuture<QuickModerationResult> quickModerate(String content, Long modelId, String language) {
        if (!aiModerationEnabled) {
            return CompletableFuture.completedFuture(QuickModerationResult.disabled());
        }

        if (content == null || content.trim().isEmpty()) {
            return CompletableFuture.completedFuture(QuickModerationResult.safe());
        }

        if (modelId == null) {
            return CompletableFuture.completedFuture(QuickModerationResult.error());
        }

        return TracingSupport.supplyAsync(() -> {
            try {
                // 1. 生成快速审核prompt
                String targetLanguage = language != null ? language : "zh-CN";
                ModerationPromptGenerator.GeneratedPrompt generatedPrompt = 
                    promptGenerator.generateQuickModerationPrompt(content, targetLanguage);
                
                String promptToUse;
                if (generatedPrompt.isSuccess()) {
                    promptToUse = generatedPrompt.getPrompt();
                } else {
                    // 使用默认的快速审核逻辑
                    log.warn("Failed to generate quick prompt, using default: {}", generatedPrompt.getErrorMessage());
                    return executeDefaultQuickModerate(content, modelId);
                }

                // 2. 动态获取ChatModel
                ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
                
                // 3. 动态创建ModerationAssistant
                ModerationAssistant moderationAssistant = AiServices.builder(ModerationAssistant.class)
                        .chatModel(chatModel)
                        .build();

                // 4. 执行快速审核
                long startTime = System.currentTimeMillis();
                String result = moderationAssistant.moderateWithPrompt(promptToUse);
                long processingTime = System.currentTimeMillis() - startTime;

                return parseQuickResult(result.trim().toUpperCase(), processingTime);

            } catch (Exception e) {
                log.error("Quick AI moderation failed, modelId: {}, language: {}", modelId, language, e);
                return QuickModerationResult.error();
            }
        }).completeOnTimeout(
                QuickModerationResult.timeout(), 
                Math.min(timeoutSeconds / 2, 5), 
                TimeUnit.SECONDS
        );
    }

    /**
     * 执行快速AI预检（使用指定模型，默认语言）
     */
    public CompletableFuture<QuickModerationResult> quickModerate(String content, Long modelId) {
        return quickModerate(content, modelId, null);
    }

    /**
     * 执行默认的快速审核（当prompt生成失败时的回退方案）
     */
    private QuickModerationResult executeDefaultQuickModerate(String content, Long modelId) {
        try {
            // 动态获取ChatModel
            ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
            
            // 动态创建ModerationAssistant
            ModerationAssistant moderationAssistant = AiServices.builder(ModerationAssistant.class)
                    .chatModel(chatModel)
                    .build();

            long startTime = System.currentTimeMillis();
            String result = moderationAssistant.quickModerate(content);
            long processingTime = System.currentTimeMillis() - startTime;

            return parseQuickResult(result.trim().toUpperCase(), processingTime);

        } catch (Exception e) {
            log.error("Default quick AI moderation failed, modelId: {}", modelId, e);
            return QuickModerationResult.error();
        }
    }

    /**
     * 批量审核内容（使用指定模型）
     */
    public CompletableFuture<List<AiModerationResult>> moderateBatch(List<String> contents, Long modelId) {
        if (contents == null || contents.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<CompletableFuture<AiModerationResult>> futures = contents.stream()
                .map(content -> moderateContent(content, modelId))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * 解析AI审核结果（新版本，支持生成的prompt信息）
     */
    private AiModerationResult parseAiResult(String aiResponse, long processingTime, Long modelId, ModerationPromptGenerator.GeneratedPrompt generatedPrompt) {
        try {
            // 尝试解析JSON响应
            if (aiResponse.contains("{") && aiResponse.contains("}")) {
                // 提取JSON部分
                int startIndex = aiResponse.indexOf('{');
                int endIndex = aiResponse.lastIndexOf('}') + 1;
                String jsonPart = aiResponse.substring(startIndex, endIndex);

                // 解析JSON响应，包含生成的prompt信息
                return parseJsonResponse(jsonPart, processingTime, modelId, generatedPrompt);
            } else {
                // 如果不是JSON格式，使用规则解析
                return parseTextResponse(aiResponse, processingTime, modelId, generatedPrompt);
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", aiResponse, e);
            return AiModerationResult.error("Failed to parse AI response");
        }
    }

    /**
     * 解析AI审核结果（保持向后兼容性）
     */
    private AiModerationResult parseAiResult(String aiResponse, long processingTime, Long modelId) {
        return parseAiResult(aiResponse, processingTime, modelId, null);
    }

    /**
     * 解析JSON格式的AI响应（新版本，支持生成的prompt信息）
     */
    private AiModerationResult parseJsonResponse(String jsonResponse, long processingTime, Long modelId, ModerationPromptGenerator.GeneratedPrompt generatedPrompt) {
        // 简化的JSON解析逻辑，实际应该使用Jackson
        try {
            ModerationResult result = ModerationResult.APPROVED;
            SeverityLevel riskLevel = SeverityLevel.LOW;
            double confidence = 0.9;
            List<ModerationRecord.ModerationViolation> violations = new ArrayList<>();
            String reasoning = "AI analysis completed";

            if (jsonResponse.contains("\"REJECTED\"")) {
                result = ModerationResult.REJECTED;
                riskLevel = SeverityLevel.HIGH;
            } else if (jsonResponse.contains("\"NEEDS_REVIEW\"")) {
                result = ModerationResult.NEEDS_REVIEW;
                riskLevel = SeverityLevel.MEDIUM;
            }

            if (jsonResponse.contains("\"CRITICAL\"")) {
                riskLevel = SeverityLevel.CRITICAL;
            } else if (jsonResponse.contains("\"HIGH\"")) {
                riskLevel = SeverityLevel.HIGH;
            } else if (jsonResponse.contains("\"MEDIUM\"")) {
                riskLevel = SeverityLevel.MEDIUM;
            }

            AiModerationResult.Builder builder = AiModerationResult.builder()
                    .result(result)
                    .riskLevel(riskLevel)
                    .confidence(BigDecimal.valueOf(confidence))
                    .violations(violations)
                    .reasoning(reasoning)
                    .processingTimeMs(processingTime)
                    .modelUsed("Model-" + modelId)
                    .rawResponse(jsonResponse)
                    .success(true);

            // 添加生成的prompt信息
            if (generatedPrompt != null) {
                // 暂时记录在reasoning中，待AiModerationResult类更新后修改
                String enhancedReasoning = reasoning;
                if (generatedPrompt.getPolicy() != null) {
                    enhancedReasoning += " [Policy: " + generatedPrompt.getPolicy().getCode() + "]";
                }
                if (generatedPrompt.getTemplate() != null) {
                    enhancedReasoning += " [Template: " + generatedPrompt.getTemplate().getCode() + "]";
                }
                if (generatedPrompt.isFallback()) {
                    enhancedReasoning += " [Fallback]";
                }
                builder.reasoning(enhancedReasoning);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Failed to parse JSON response: {}", jsonResponse, e);
            return AiModerationResult.error("JSON parsing failed");
        }
    }

    /**
     * 解析文本格式的AI响应（新版本，支持生成的prompt信息）
     */
    private AiModerationResult parseTextResponse(String textResponse, long processingTime, Long modelId, ModerationPromptGenerator.GeneratedPrompt generatedPrompt) {
        String lowerResponse = textResponse.toLowerCase();
        
        ModerationResult result = ModerationResult.APPROVED;
        SeverityLevel riskLevel = SeverityLevel.LOW;
        double confidence = 0.8;

        if (lowerResponse.contains("rejected") || lowerResponse.contains("违规") || lowerResponse.contains("不当")) {
            result = ModerationResult.REJECTED;
            riskLevel = SeverityLevel.HIGH;
        } else if (lowerResponse.contains("review") || lowerResponse.contains("审核") || lowerResponse.contains("可疑")) {
            result = ModerationResult.NEEDS_REVIEW;
            riskLevel = SeverityLevel.MEDIUM;
        }

        AiModerationResult.Builder builder = AiModerationResult.builder()
                .result(result)
                .riskLevel(riskLevel)
                .confidence(BigDecimal.valueOf(confidence))
                .violations(new ArrayList<>())
                .reasoning(textResponse)
                .processingTimeMs(processingTime)
                .modelUsed("Model-" + modelId)
                .rawResponse(textResponse)
                .success(true);

        // 添加生成的prompt信息
        if (generatedPrompt != null) {
            // 暂时记录在reasoning中，待AiModerationResult类更新后修改
            String enhancedReasoning = textResponse;
            if (generatedPrompt.getPolicy() != null) {
                enhancedReasoning += " [Policy: " + generatedPrompt.getPolicy().getCode() + "]";
            }
            if (generatedPrompt.getTemplate() != null) {
                enhancedReasoning += " [Template: " + generatedPrompt.getTemplate().getCode() + "]";
            }
            if (generatedPrompt.isFallback()) {
                enhancedReasoning += " [Fallback]";
            }
            builder.reasoning(enhancedReasoning);
        }

        return builder.build();
    }

    /**
     * 解析快速审核结果
     */
    private QuickModerationResult parseQuickResult(String result, long processingTime) {
        return switch (result) {
            case "SAFE" -> QuickModerationResult.safe(processingTime);
            case "UNSAFE" -> QuickModerationResult.needsReview(processingTime);
            case "BLOCKED" -> QuickModerationResult.blocked(processingTime);
            default -> QuickModerationResult.needsReview(processingTime); // 默认需要审核
        };
    }

    /**
     * AI审核结果封装类
     */
    public static class AiModerationResult {
        private final ModerationResult result;
        private final SeverityLevel riskLevel;
        private final BigDecimal confidence;
        private final List<ModerationRecord.ModerationViolation> violations;
        private final String reasoning;
        private final long processingTimeMs;
        private final String modelUsed;
        private final String rawResponse;
        private final boolean success;
        private final String errorMessage;

        private AiModerationResult(Builder builder) {
            this.result = builder.result;
            this.riskLevel = builder.riskLevel;
            this.confidence = builder.confidence;
            this.violations = builder.violations;
            this.reasoning = builder.reasoning;
            this.processingTimeMs = builder.processingTimeMs;
            this.modelUsed = builder.modelUsed;
            this.rawResponse = builder.rawResponse;
            this.success = builder.success;
            this.errorMessage = builder.errorMessage;
        }

        public static Builder builder() { return new Builder(); }
        
        public static AiModerationResult approved(String reasoning, double confidence) {
            return builder()
                    .result(ModerationResult.APPROVED)
                    .riskLevel(SeverityLevel.LOW)
                    .confidence(BigDecimal.valueOf(confidence))
                    .reasoning(reasoning)
                    .success(true)
                    .build();
        }

        public static AiModerationResult error(String errorMessage) {
            return builder()
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();
        }

        public static AiModerationResult disabled(String reason) {
            return builder()
                    .result(ModerationResult.APPROVED)
                    .reasoning(reason)
                    .success(false)
                    .build();
        }

        public static AiModerationResult timeout(String message) {
            return builder()
                    .result(ModerationResult.NEEDS_REVIEW)
                    .reasoning(message)
                    .success(false)
                    .build();
        }

        // Getters
        public ModerationResult getResult() { return result; }
        public SeverityLevel getRiskLevel() { return riskLevel; }
        public BigDecimal getConfidence() { return confidence; }
        public List<ModerationRecord.ModerationViolation> getViolations() { return violations; }
        public String getReasoning() { return reasoning; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public String getModelUsed() { return modelUsed; }
        public String getRawResponse() { return rawResponse; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }

        public static class Builder {
            private ModerationResult result;
            private SeverityLevel riskLevel;
            private BigDecimal confidence;
            private List<ModerationRecord.ModerationViolation> violations = new ArrayList<>();
            private String reasoning;
            private long processingTimeMs;
            private String modelUsed;
            private String rawResponse;
            private boolean success;
            private String errorMessage;

            public Builder result(ModerationResult result) { this.result = result; return this; }
            public Builder riskLevel(SeverityLevel riskLevel) { this.riskLevel = riskLevel; return this; }
            public Builder confidence(BigDecimal confidence) { this.confidence = confidence; return this; }
            public Builder violations(List<ModerationRecord.ModerationViolation> violations) { this.violations = violations; return this; }
            public Builder reasoning(String reasoning) { this.reasoning = reasoning; return this; }
            public Builder processingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }
            public Builder modelUsed(String modelUsed) { this.modelUsed = modelUsed; return this; }
            public Builder rawResponse(String rawResponse) { this.rawResponse = rawResponse; return this; }
            public Builder success(boolean success) { this.success = success; return this; }
            public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
            public AiModerationResult build() { return new AiModerationResult(this); }
        }
    }

    /**
     * 快速审核结果封装类
     */
    public static class QuickModerationResult {
        public enum QuickResult {
            SAFE, NEEDS_REVIEW, BLOCKED, TIMEOUT, ERROR, DISABLED
        }

        private final QuickResult result;
        private final long processingTimeMs;

        private QuickModerationResult(QuickResult result, long processingTimeMs) {
            this.result = result;
            this.processingTimeMs = processingTimeMs;
        }

        public static QuickModerationResult safe() { return new QuickModerationResult(QuickResult.SAFE, 0); }
        public static QuickModerationResult safe(long processingTime) { return new QuickModerationResult(QuickResult.SAFE, processingTime); }
        // Factory method for needs review (removed duplicate)
        public static QuickModerationResult needsReview(long processingTime) { return new QuickModerationResult(QuickResult.NEEDS_REVIEW, processingTime); }
        public static QuickModerationResult blocked() { return new QuickModerationResult(QuickResult.BLOCKED, 0); }
        public static QuickModerationResult blocked(long processingTime) { return new QuickModerationResult(QuickResult.BLOCKED, processingTime); }
        public static QuickModerationResult timeout() { return new QuickModerationResult(QuickResult.TIMEOUT, 0); }
        public static QuickModerationResult error() { return new QuickModerationResult(QuickResult.ERROR, 0); }
        public static QuickModerationResult disabled() { return new QuickModerationResult(QuickResult.DISABLED, 0); }

        public QuickResult getResult() { return result; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public boolean isSafe() { return result == QuickResult.SAFE; }
        public boolean requiresReview() { return result == QuickResult.NEEDS_REVIEW; }
        public boolean isBlocked() { return result == QuickResult.BLOCKED; }
    }
}
