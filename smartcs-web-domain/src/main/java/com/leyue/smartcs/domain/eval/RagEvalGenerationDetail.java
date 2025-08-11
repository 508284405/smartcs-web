package com.leyue.smartcs.domain.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * RAG评估生成详情领域模型
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalGenerationDetail {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 测试用例ID
     */
    private String caseId;
    
    /**
     * 原始查询
     */
    private String queryOriginal;
    
    /**
     * 注入的上下文信息
     */
    private List<InjectedContext> injectedContexts;
    
    // ====== 提示构建 ======
    
    /**
     * 最终构建的提示词
     */
    private String finalPrompt;
    
    /**
     * 使用的提示模板
     */
    private String promptTemplate;
    
    /**
     * 提示Token数量
     */
    private Integer promptTokens;
    
    /**
     * 上下文注入策略
     */
    private String contextInjectionStrategy;
    
    // ====== 生成结果 ======
    
    /**
     * 生成的答案
     */
    private String generatedAnswer;
    
    /**
     * 答案Token数量
     */
    private Integer answerTokens;
    
    /**
     * 生成延迟（毫秒）
     */
    private Integer generationLatencyMs;
    
    /**
     * 使用的生成模型
     */
    private String modelUsed;
    
    /**
     * 模型参数
     */
    private Map<String, Object> modelParams;
    
    /**
     * 是否启用流式生成：0-否，1-是
     */
    private Integer streamingEnabled;
    
    // ====== 引用和依据 ======
    
    /**
     * 引用信息
     */
    private List<Citation> citations;
    
    /**
     * 支持证据
     */
    private List<SupportingEvidence> supportingEvidence;
    
    /**
     * 引用准确性
     */
    private BigDecimal citationAccuracy;
    
    /**
     * 证据对齐度
     */
    private BigDecimal evidenceAlignment;
    
    // ====== RAGAS生成指标 ======
    
    /**
     * RAGAS Faithfulness分数
     */
    private BigDecimal faithfulnessScore;
    
    /**
     * RAGAS Answer Relevancy分数
     */
    private BigDecimal answerRelevancyScore;
    
    /**
     * 基于事实程度分数
     */
    private BigDecimal groundednessScore;
    
    // ====== 质量评估指标 ======
    
    /**
     * 事实正确性分数
     */
    private BigDecimal factualCorrectnessScore;
    
    /**
     * 完整性分数
     */
    private BigDecimal completenessScore;
    
    /**
     * 简洁性分数
     */
    private BigDecimal concisenessScore;
    
    /**
     * 连贯性分数
     */
    private BigDecimal coherenceScore;
    
    /**
     * 流畅性分数
     */
    private BigDecimal fluencyScore;
    
    // ====== 引用一致性检查 ======
    
    /**
     * 引用一致性分数
     */
    private BigDecimal citationConsistencyScore;
    
    /**
     * 是否检测到幻觉：0-否，1-是
     */
    private Integer hallucinationDetected;
    
    /**
     * 幻觉严重程度：low, medium, high
     */
    private String hallucinationSeverity;
    
    /**
     * 不支持的声明列表
     */
    private List<UnsupportedClaim> unsupportedClaims;
    
    // ====== 与期望答案的比较 ======
    
    /**
     * 期望的答案（来自测试用例）
     */
    private String expectedAnswer;
    
    /**
     * 语义相似度
     */
    private BigDecimal semanticSimilarity;
    
    /**
     * BLEU分数
     */
    private BigDecimal bleuScore;
    
    /**
     * ROUGE-L分数
     */
    private BigDecimal rougeLScore;
    
    /**
     * BERTScore F1分数
     */
    private BigDecimal bertscoreF1;
    
    // ====== 成本分析 ======
    
    /**
     * 总Token数量
     */
    private Integer totalTokens;
    
    /**
     * 成本（美元）
     */
    private BigDecimal costUsd;
    
    /**
     * 成本分解
     */
    private Map<String, Object> costBreakdown;
    
    // ====== 错误和异常 ======
    
    /**
     * 是否有错误：0-否，1-是
     */
    private Integer hasError;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误阶段：prompt_build, generation, evaluation
     */
    private String errorStage;
    
    // ====== 调试和分析 ======
    
    /**
     * 生成步骤详情（流式生成时的中间结果）
     */
    private List<GenerationStep> generationSteps;
    
    /**
     * 注意力权重（如果可用）
     */
    private Map<String, Object> attentionWeights;
    
    /**
     * 调试信息
     */
    private Map<String, Object> debugInfo;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 检查是否有错误
     */
    public boolean hasError() {
        return hasError != null && hasError == 1;
    }
    
    /**
     * 检查是否启用流式生成
     */
    public boolean isStreamingEnabled() {
        return streamingEnabled != null && streamingEnabled == 1;
    }
    
    /**
     * 检查是否检测到幻觉
     */
    public boolean isHallucinationDetected() {
        return hallucinationDetected != null && hallucinationDetected == 1;
    }
    
    /**
     * 检查是否有引用
     */
    public boolean hasCitations() {
        return citations != null && !citations.isEmpty();
    }
    
    /**
     * 计算Token效率（答案Token/总Token）
     */
    public Double getTokenEfficiency() {
        if (answerTokens == null || totalTokens == null || totalTokens == 0) {
            return null;
        }
        return (double) answerTokens / totalTokens;
    }
    
    /**
     * 注入的上下文
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InjectedContext {
        /**
         * 上下文ID
         */
        private String contextId;
        
        /**
         * 上下文内容
         */
        private String content;
        
        /**
         * 在提示中的位置
         */
        private Integer position;
        
        /**
         * 重要性权重
         */
        private Double weight;
        
        /**
         * 来源文档ID
         */
        private String sourceDocumentId;
    }
    
    /**
     * 引用信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Citation {
        /**
         * 引用的文档ID
         */
        private String documentId;
        
        /**
         * 引用的内容片段
         */
        private String citedContent;
        
        /**
         * 在答案中的位置
         */
        private Integer position;
        
        /**
         * 置信度
         */
        private Double confidence;
        
        /**
         * 引用类型
         */
        private String citationType;
    }
    
    /**
     * 支持证据
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SupportingEvidence {
        /**
         * 证据ID
         */
        private String evidenceId;
        
        /**
         * 证据内容
         */
        private String content;
        
        /**
         * 支持程度
         */
        private Double supportLevel;
        
        /**
         * 证据类型
         */
        private String evidenceType;
        
        /**
         * 相关性分数
         */
        private Double relevanceScore;
    }
    
    /**
     * 不支持的声明
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnsupportedClaim {
        /**
         * 声明内容
         */
        private String claim;
        
        /**
         * 在答案中的位置
         */
        private Integer position;
        
        /**
         * 严重程度：low, medium, high
         */
        private String severity;
        
        /**
         * 原因说明
         */
        private String reason;
    }
    
    /**
     * 生成步骤
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerationStep {
        /**
         * 步骤序号
         */
        private Integer stepNumber;
        
        /**
         * 步骤类型
         */
        private String stepType;
        
        /**
         * 中间结果
         */
        private String intermediateResult;
        
        /**
         * 步骤耗时（毫秒）
         */
        private Integer duration;
        
        /**
         * 置信度
         */
        private Double confidence;
    }
}