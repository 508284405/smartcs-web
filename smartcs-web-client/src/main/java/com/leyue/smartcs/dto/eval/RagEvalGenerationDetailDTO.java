package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * RAG评估生成详情DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalGenerationDetailDTO {
    
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
    private List<InjectedContextDTO> injectedContexts;
    
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
    private List<CitationDTO> citations;
    
    /**
     * 支持证据
     */
    private List<SupportingEvidenceDTO> supportingEvidence;
    
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
     * 引用完整性分数
     */
    private BigDecimal citationCompletenessScore;
    
    /**
     * 引用准确性分数
     */
    private BigDecimal citationAccuracyScore;
    
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
     * 错误阶段：prompt_building, generation, evaluation
     */
    private String errorStage;
    
    // ====== 调试和元数据 ======
    
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
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    // ====== 内部DTO类 ======
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InjectedContextDTO {
        private String contextId;
        private String content;
        private String sourceDocument;
        private BigDecimal relevanceScore;
        private Map<String, Object> metadata;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CitationDTO {
        private String citationId;
        private String sourceDocument;
        private String content;
        private Integer startPosition;
        private Integer endPosition;
        private BigDecimal confidence;
        private Map<String, Object> metadata;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SupportingEvidenceDTO {
        private String evidenceId;
        private String content;
        private String sourceDocument;
        private BigDecimal relevanceScore;
        private String evidenceType;
        private Map<String, Object> metadata;
    }
}
