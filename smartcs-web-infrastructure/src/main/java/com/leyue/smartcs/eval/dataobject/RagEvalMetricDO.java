package com.leyue.smartcs.eval.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * RAG评估指标汇总数据对象
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@TableName(value = "t_rag_eval_metric", autoResultMap = true)
public class RagEvalMetricDO {
    
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
     * 指标类别：retrieval, generation, efficiency, robustness
     */
    private String metricCategory;
    
    // ====== 检索指标 ======
    
    /**
     * Precision@1
     */
    private BigDecimal precisionAt1;
    
    /**
     * Precision@3
     */
    private BigDecimal precisionAt3;
    
    /**
     * Precision@5
     */
    private BigDecimal precisionAt5;
    
    /**
     * Recall@1
     */
    private BigDecimal recallAt1;
    
    /**
     * Recall@3
     */
    private BigDecimal recallAt3;
    
    /**
     * Recall@5
     */
    private BigDecimal recallAt5;
    
    /**
     * 平均倒数排名（Mean Reciprocal Rank）
     */
    private BigDecimal mrr;
    
    /**
     * NDCG@3
     */
    private BigDecimal ndcgAt3;
    
    /**
     * NDCG@5
     */
    private BigDecimal ndcgAt5;
    
    /**
     * RAGAS Context Precision
     */
    private BigDecimal contextPrecision;
    
    /**
     * RAGAS Context Recall
     */
    private BigDecimal contextRecall;
    
    /**
     * 重排序改进率
     */
    private BigDecimal rerankImprovement;
    
    // ====== 生成指标 ======
    
    /**
     * RAGAS Faithfulness（忠实度）
     */
    private BigDecimal faithfulness;
    
    /**
     * RAGAS Answer Relevancy（答案相关性）
     */
    private BigDecimal answerRelevancy;
    
    /**
     * 引用一致性
     */
    private BigDecimal citationConsistency;
    
    /**
     * 完整性
     */
    private BigDecimal completeness;
    
    /**
     * 简洁性
     */
    private BigDecimal conciseness;
    
    /**
     * 基于事实程度
     */
    private BigDecimal groundedness;
    
    /**
     * 事实正确性
     */
    private BigDecimal factualCorrectness;
    
    // ====== 效率指标 ======
    
    /**
     * 平均检索延迟（毫秒）
     */
    private BigDecimal avgRetrievalLatencyMs;
    
    /**
     * 平均重排序延迟（毫秒）
     */
    private BigDecimal avgRerankLatencyMs;
    
    /**
     * 平均生成延迟（毫秒）
     */
    private BigDecimal avgGenerationLatencyMs;
    
    /**
     * 平均端到端延迟（毫秒）
     */
    private BigDecimal avgE2eLatencyMs;
    
    /**
     * 平均输入Token数
     */
    private BigDecimal avgInputTokens;
    
    /**
     * 平均输出Token数
     */
    private BigDecimal avgOutputTokens;
    
    /**
     * 平均成本（美元）
     */
    private BigDecimal avgCostUsd;
    
    // ====== 鲁棒性指标 ======
    
    /**
     * 鲁棒性得分
     */
    private BigDecimal robustnessScore;
    
    /**
     * 方差阈值
     */
    private BigDecimal varianceThreshold;
    
    /**
     * 扰动一致性
     */
    private BigDecimal perturbationConsistency;
    
    // ====== 聚合统计 ======
    
    /**
     * 样本数量
     */
    private Integer sampleCount;
    
    /**
     * 成功率
     */
    private BigDecimal successRate;
    
    /**
     * 错误率
     */
    private BigDecimal errorRate;
    
    // ====== 详细数据 ======
    
    /**
     * 详细指标数据（包含分布、百分位数等）
     */
    private Map<String, Object> detailedMetrics;
    
    /**
     * 置信区间
     */
    private Map<String, Object> confidenceIntervals;
    
    /**
     * 统计检验结果
     */
    private Map<String, Object> statisticalTests;
    
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