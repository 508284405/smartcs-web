package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG评估趋势分析查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalTrendAnalysisQry {
    
    /**
     * 数据集ID列表
     */
    private List<String> datasetIds;
    
    /**
     * 模型ID列表
     */
    private List<Long> modelIds;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 时间粒度：hour, day, week, month
     */
    private String timeGranularity;
    
    /**
     * 关注的指标列表
     */
    private List<String> focusMetrics;
    
    /**
     * 趋势类型：linear, polynomial, exponential
     */
    private String trendType;
    
    /**
     * 是否包含季节性分析
     */
    private Boolean includeSeasonality;
    
    /**
     * 是否包含异常检测
     */
    private Boolean includeAnomalyDetection;
    
    /**
     * 是否包含预测
     */
    private Boolean includeForecasting;
    
    /**
     * 预测步数
     */
    private Integer forecastSteps;
    
    /**
     * 置信水平
     */
    private Double confidenceLevel;
}
