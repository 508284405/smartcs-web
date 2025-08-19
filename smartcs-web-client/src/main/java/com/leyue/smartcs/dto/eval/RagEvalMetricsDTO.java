package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAG评估指标DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalMetricsDTO {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 检索质量分数
     */
    private Double retrievalScore;
    
    /**
     * 生成质量分数
     */
    private Double generationScore;
    
    /**
     * 整体质量分数
     */
    private Double overallScore;
    
    /**
     * 准确性分数
     */
    private Double accuracyScore;
    
    /**
     * 完整性分数
     */
    private Double completenessScore;
    
    /**
     * 相关性分数
     */
    private Double relevanceScore;
    
    /**
     * 一致性分数
     */
    private Double consistencyScore;
    
    /**
     * 流畅性分数
     */
    private Double fluencyScore;
    
    /**
     * 详细指标
     */
    private Map<String, Object> detailedMetrics;
    
    /**
     * 指标计算时间
     */
    private Long calculationTime;
    
    /**
     * 指标版本
     */
    private String metricsVersion;
}
