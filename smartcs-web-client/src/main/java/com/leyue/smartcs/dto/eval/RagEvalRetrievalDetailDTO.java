package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * RAG评估检索详情DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRetrievalDetailDTO {
    
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
     * 重写后的查询
     */
    private String queryRewritten;
    
    /**
     * 查询向量化耗时（毫秒）
     */
    private Integer queryEmbeddingTimeMs;
    
    /**
     * 检索候选结果
     */
    private List<RetrievalCandidateDTO> retrievalCandidates;
    
    /**
     * 检索分数详情
     */
    private Map<String, Object> retrievalScores;
    
    /**
     * 检索延迟（毫秒）
     */
    private Integer retrievalLatencyMs;
    
    /**
     * 使用的检索模型
     */
    private String retrievalModelUsed;
    
    /**
     * 检索参数
     */
    private Map<String, Object> retrievalParams;
    
    /**
     * 重排序后的候选结果
     */
    private List<RetrievalCandidateDTO> rerankCandidates;
    
    /**
     * 重排序分数
     */
    private Map<String, Object> rerankScores;
    
    /**
     * 重排序延迟（毫秒）
     */
    private Integer rerankLatencyMs;
    
    /**
     * 使用的重排序模型
     */
    private String rerankModelUsed;
    
    /**
     * 重排序参数
     */
    private Map<String, Object> rerankParams;
    
    /**
     * 重排序改进程度
     */
    private BigDecimal rerankImprovement;
    
    /**
     * 最终选择的上下文
     */
    private List<ContextFragmentDTO> finalContexts;
    
    /**
     * 过滤掉的候选项
     */
    private List<RetrievalCandidateDTO> filteredCandidates;
    
    /**
     * 上下文压缩比例
     */
    private BigDecimal contextCompressionRatio;
    
    // ====== 评估指标（针对此用例） ======
    
    /**
     * 单样本Precision@1
     */
    private BigDecimal precisionAt1;
    
    /**
     * 单样本Precision@3
     */
    private BigDecimal precisionAt3;
    
    /**
     * 单样本Precision@5
     */
    private BigDecimal precisionAt5;
    
    /**
     * 单样本Recall@1
     */
    private BigDecimal recallAt1;
    
    /**
     * 单样本Recall@3
     */
    private BigDecimal recallAt3;
    
    /**
     * 单样本Recall@5
     */
    private BigDecimal recallAt5;
    
    /**
     * 倒数排名
     */
    private BigDecimal reciprocalRank;
    
    /**
     * NDCG分数
     */
    private BigDecimal ndcg;
    
    /**
     * RAGAS Context Precision分数
     */
    private BigDecimal contextPrecisionScore;
    
    /**
     * RAGAS Context Recall分数
     */
    private BigDecimal contextRecallScore;
    
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
     * 错误阶段：embedding, retrieval, rerank, filter
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
    public static class RetrievalCandidateDTO {
        private String documentId;
        private String content;
        private BigDecimal similarityScore;
        private Map<String, Object> metadata;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContextFragmentDTO {
        private String fragmentId;
        private String content;
        private String sourceDocument;
        private BigDecimal relevanceScore;
        private Map<String, Object> metadata;
    }
}
