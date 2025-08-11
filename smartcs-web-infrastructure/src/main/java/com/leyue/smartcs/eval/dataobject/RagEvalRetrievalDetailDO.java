package com.leyue.smartcs.eval.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * RAG评估检索详情数据对象
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@TableName(value = "t_rag_eval_retrieval_detail", autoResultMap = true)
public class RagEvalRetrievalDetailDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 检索候选结果（TopK文档，包含ID、文本片段、向量相似度分数）
     */
    private String retrievalCandidates;
    
    /**
     * 检索分数详情
     */
    private String retrievalScores;
    
    /**
     * 检索延迟（毫秒）
     */
    private Integer retrievalLatencyMs;
    
    /**
     * 使用的检索模型
     */
    private String retrievalModelUsed;
    
    /**
     * 检索参数（TopK、阈值等）
     */
    private String retrievalParams;
    
    /**
     * 重排序后的候选结果
     */
    private String rerankCandidates;
    
    /**
     * 重排序分数
     */
    private String rerankScores;
    
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
    private String rerankParams;
    
    /**
     * 重排序改进程度
     */
    private BigDecimal rerankImprovement;
    
    /**
     * 最终选择的上下文
     */
    private String finalContexts;
    
    /**
     * 过滤掉的候选项
     */
    private String filteredCandidates;
    
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
}