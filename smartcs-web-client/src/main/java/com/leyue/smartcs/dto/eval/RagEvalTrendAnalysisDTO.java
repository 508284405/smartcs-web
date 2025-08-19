package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估趋势分析DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalTrendAnalysisDTO {
    
    /**
     * 时间范围
     */
    private String timeRange;
    
    /**
     * 趋势方向：上升、下降、稳定
     */
    private String trendDirection;
    
    /**
     * 趋势强度：强、中、弱
     */
    private String trendStrength;
    
    /**
     * 趋势摘要
     */
    private String trendSummary;
    
    /**
     * 时间序列数据
     */
    private List<TimeSeriesPoint> timeSeriesData;
    
    /**
     * 各指标的趋势
     */
    private Map<String, MetricTrend> metricTrends;
    
    /**
     * 季节性分析
     */
    private SeasonalAnalysis seasonalAnalysis;
    
    /**
     * 异常点检测
     */
    private List<AnomalyPoint> anomalyPoints;
    
    /**
     * 预测数据
     */
    private List<ForecastPoint> forecastData;
    
    /**
     * 趋势变化点
     */
    private List<ChangePoint> changePoints;
    
    /**
     * 时间序列数据点
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSeriesPoint {
        
        /**
         * 时间戳
         */
        private Long timestamp;
        
        /**
         * 时间标签
         */
        private String timeLabel;
        
        /**
         * 指标值
         */
        private Double value;
        
        /**
         * 是否异常
         */
        private Boolean isAnomaly;
    }
    
    /**
     * 指标趋势信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetricTrend {
        
        /**
         * 指标名称
         */
        private String metricName;
        
        /**
         * 趋势方向
         */
        private String direction;
        
        /**
         * 趋势斜率
         */
        private Double slope;
        
        /**
         * R平方值
         */
        private Double rSquared;
        
        /**
         * 趋势显著性
         */
        private Boolean isSignificant;
    }
    
    /**
     * 季节性分析
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeasonalAnalysis {
        
        /**
         * 季节性周期
         */
        private String seasonalPeriod;
        
        /**
         * 季节性强度
         */
        private Double seasonalStrength;
        
        /**
         * 季节性模式
         */
        private List<Double> seasonalPattern;
        
        /**
         * 是否检测到季节性
         */
        private Boolean hasSeasonality;
    }
    
    /**
     * 异常点信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnomalyPoint {
        
        /**
         * 时间戳
         */
        private Long timestamp;
        
        /**
         * 异常值
         */
        private Double value;
        
        /**
         * 异常分数
         */
        private Double anomalyScore;
        
        /**
         * 异常类型
         */
        private String anomalyType;
        
        /**
         * 异常描述
         */
        private String description;
    }
    
    /**
     * 预测点信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ForecastPoint {
        
        /**
         * 时间戳
         */
        private Long timestamp;
        
        /**
         * 预测值
         */
        private Double predictedValue;
        
        /**
         * 预测区间下限
         */
        private Double lowerBound;
        
        /**
         * 预测区间上限
         */
        private Double upperBound;
        
        /**
         * 预测置信度
         */
        private Double confidence;
    }
    
    /**
     * 变化点信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChangePoint {
        
        /**
         * 时间戳
         */
        private Long timestamp;
        
        /**
         * 变化类型
         */
        private String changeType;
        
        /**
         * 变化强度
         */
        private Double changeIntensity;
        
        /**
         * 变化描述
         */
        private String description;
    }
}
