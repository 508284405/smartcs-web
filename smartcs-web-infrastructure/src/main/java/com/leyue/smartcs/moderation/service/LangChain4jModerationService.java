package com.leyue.smartcs.moderation.service;

import com.leyue.smartcs.domain.moderation.ModerationRecord;
import com.leyue.smartcs.domain.moderation.enums.ModerationResult;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.model.ai.DynamicModelManager;
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

/**
 * 基于LangChain4j的AI内容审核服务
 * 使用LLM进行智能内容审核和风险评估
 * 支持根据modelId动态选择AI模型
 */
@Slf4j
@Service
public class LangChain4jModerationService {

    private final DynamicModelManager dynamicModelManager;

    @Value("${moderation.ai.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${moderation.ai.enabled:true}")
    private boolean aiModerationEnabled;

    @Value("${moderation.ai.model-name:gpt-3.5-turbo}")
    private String defaultModelName;

    public LangChain4jModerationService(DynamicModelManager dynamicModelManager) {
        this.dynamicModelManager = dynamicModelManager;
    }

    /**
     * AI审核助手接口
     */
    interface ModerationAssistant {
        @UserMessage("""
            请对以下内容进行安全审核，评估其是否存在违规风险。

            审核内容：
            {{content}}

            请从以下维度进行评估：
            1. 仇恨言论 - 基于种族、性别、宗教等的歧视性言论
            2. 骚扰威胁 - 对个人或群体的威胁、恐吓、霸凌行为
            3. 性内容 - 明确的性内容或成人内容
            4. 暴力内容 - 描述或宣扬暴力行为
            5. 自我伤害 - 鼓励自杀或自我伤害的内容
            6. 危险活动 - 危险或非法活动的指导
            7. 垃圾信息 - 垃圾信息、广告推广
            8. 隐私泄露 - 包含个人隐私信息

            请返回JSON格式的审核结果，包含：
            {
              "result": "APPROVED/REJECTED/NEEDS_REVIEW",
              "riskLevel": "LOW/MEDIUM/HIGH/CRITICAL",
              "confidence": 0.85,
              "violations": [
                {
                  "category": "违规分类",
                  "confidence": 0.9,
                  "reason": "具体原因"
                }
              ],
              "reasoning": "详细分析说明"
            }
            """)
        String moderateContent(String content);

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
     * 执行AI内容审核（使用指定模型）
     */
    public CompletableFuture<AiModerationResult> moderateContent(String content, Long modelId) {
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

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 动态获取ChatModel
                ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
                
                // 动态创建ModerationAssistant
                ModerationAssistant moderationAssistant = AiServices.builder(ModerationAssistant.class)
                        .chatModel(chatModel)
                        .build();

                long startTime = System.currentTimeMillis();
                String result = moderationAssistant.moderateContent(content);
                long processingTime = System.currentTimeMillis() - startTime;

                AiModerationResult moderationResult = parseAiResult(result, processingTime, modelId);
                log.debug("AI moderation completed in {}ms, modelId: {}, result: {}", 
                         processingTime, modelId, moderationResult.getResult());
                return moderationResult;

            } catch (Exception e) {
                log.error("AI moderation failed for content length: {}, modelId: {}", content.length(), modelId, e);
                return AiModerationResult.error("AI moderation failed: " + e.getMessage());
            }
        }).completeOnTimeout(
                AiModerationResult.timeout("AI moderation timeout"), 
                timeoutSeconds, 
                TimeUnit.SECONDS
        );
    }

    /**
     * 执行快速AI预检（使用指定模型）
     */
    public CompletableFuture<QuickModerationResult> quickModerate(String content, Long modelId) {
        if (!aiModerationEnabled) {
            return CompletableFuture.completedFuture(QuickModerationResult.disabled());
        }

        if (content == null || content.trim().isEmpty()) {
            return CompletableFuture.completedFuture(QuickModerationResult.safe());
        }

        if (modelId == null) {
            return CompletableFuture.completedFuture(QuickModerationResult.error());
        }

        return CompletableFuture.supplyAsync(() -> {
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
                log.error("Quick AI moderation failed, modelId: {}", modelId, e);
                return QuickModerationResult.error();
            }
        }).completeOnTimeout(
                QuickModerationResult.timeout(), 
                Math.min(timeoutSeconds / 2, 5), 
                TimeUnit.SECONDS
        );
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
     * 解析AI审核结果
     */
    private AiModerationResult parseAiResult(String aiResponse, long processingTime, Long modelId) {
        try {
            // 尝试解析JSON响应
            if (aiResponse.contains("{") && aiResponse.contains("}")) {
                // 提取JSON部分
                int startIndex = aiResponse.indexOf('{');
                int endIndex = aiResponse.lastIndexOf('}') + 1;
                String jsonPart = aiResponse.substring(startIndex, endIndex);

                // 这里应该使用Jackson解析，为简化演示使用基础解析
                return parseJsonResponse(jsonPart, processingTime, modelId);
            } else {
                // 如果不是JSON格式，使用规则解析
                return parseTextResponse(aiResponse, processingTime, modelId);
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", aiResponse, e);
            return AiModerationResult.error("Failed to parse AI response");
        }
    }

    /**
     * 解析JSON格式的AI响应
     */
    private AiModerationResult parseJsonResponse(String jsonResponse, long processingTime, Long modelId) {
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

            return AiModerationResult.builder()
                    .result(result)
                    .riskLevel(riskLevel)
                    .confidence(BigDecimal.valueOf(confidence))
                    .violations(violations)
                    .reasoning(reasoning)
                    .processingTimeMs(processingTime)
                    .modelUsed("Model-" + modelId)
                    .rawResponse(jsonResponse)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse JSON response: {}", jsonResponse, e);
            return AiModerationResult.error("JSON parsing failed");
        }
    }

    /**
     * 解析文本格式的AI响应
     */
    private AiModerationResult parseTextResponse(String textResponse, long processingTime, Long modelId) {
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

        return AiModerationResult.builder()
                .result(result)
                .riskLevel(riskLevel)
                .confidence(BigDecimal.valueOf(confidence))
                .violations(new ArrayList<>())
                .reasoning(textResponse)
                .processingTimeMs(processingTime)
                .modelUsed("Model-" + modelId)
                .rawResponse(textResponse)
                .success(true)
                .build();
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