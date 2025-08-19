package com.leyue.smartcs.domain.eval;

import com.leyue.smartcs.domain.eval.enums.MetricCategory;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * RAG评估指标汇总领域模型
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalMetric {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 指标类别
     */
    private MetricCategory metricCategory;
    
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
    
    /**
     * 检查是否为检索指标
     */
    public boolean isRetrievalMetric() {
        return metricCategory == MetricCategory.RETRIEVAL;
    }
    
    /**
     * 检查是否为生成指标
     */
    public boolean isGenerationMetric() {
        return metricCategory == MetricCategory.GENERATION;
    }
    
    /**
     * 检查是否为效率指标
     */
    public boolean isEfficiencyMetric() {
        return metricCategory == MetricCategory.EFFICIENCY;
    }
    
    /**
     * 检查是否为鲁棒性指标
     */
    public boolean isRobustnessMetric() {
        return metricCategory == MetricCategory.ROBUSTNESS;
    }
    
    /**
     * 获取主要指标（根据类别返回最重要的指标）
     */
    public BigDecimal getPrimaryMetric() {
        switch (metricCategory) {
            case RETRIEVAL:
                return contextRecall; // Context Recall作为检索的主要指标
            case GENERATION:
                return faithfulness; // Faithfulness作为生成的主要指标
            case EFFICIENCY:
                return avgE2eLatencyMs; // 端到端延迟作为效率的主要指标
            case ROBUSTNESS:
                return robustnessScore; // 鲁棒性得分作为鲁棒性的主要指标
            default:
                return null;
        }
    }
    
    /**
     * 计算综合得分
     */
    public BigDecimal calculateCompositeScore() {
        // 根据不同类别计算综合得分的逻辑
        if (isRetrievalMetric()) {
            return calculateRetrievalCompositeScore();
        } else if (isGenerationMetric()) {
            return calculateGenerationCompositeScore();
        }
        return null;
    }
    
    /**
     * 计算检索综合得分
     */
    private BigDecimal calculateRetrievalCompositeScore() {
        // 权重：Context Precision 40%, Context Recall 40%, MRR 20%
        BigDecimal score = BigDecimal.ZERO;
        int validMetrics = 0;
        
        if (contextPrecision != null) {
            score = score.add(contextPrecision.multiply(BigDecimal.valueOf(0.4)));
            validMetrics++;
        }
        if (contextRecall != null) {
            score = score.add(contextRecall.multiply(BigDecimal.valueOf(0.4)));
            validMetrics++;
        }
        if (mrr != null) {
            score = score.add(mrr.multiply(BigDecimal.valueOf(0.2)));
            validMetrics++;
        }
        
        return validMetrics > 0 ? score : null;
    }
    
    /**
     * 计算生成综合得分
     */
    private BigDecimal calculateGenerationCompositeScore() {
        // 权重：Faithfulness 50%, Answer Relevancy 30%, Citation Consistency 20%
        BigDecimal score = BigDecimal.ZERO;
        int validMetrics = 0;
        
        if (faithfulness != null) {
            score = score.add(faithfulness.multiply(BigDecimal.valueOf(0.5)));
            validMetrics++;
        }
        if (answerRelevancy != null) {
            score = score.add(answerRelevancy.multiply(BigDecimal.valueOf(0.3)));
            validMetrics++;
        }
        if (citationConsistency != null) {
            score = score.add(citationConsistency.multiply(BigDecimal.valueOf(0.2)));
            validMetrics++;
        }
        
        return validMetrics > 0 ? score : null;
    }
}