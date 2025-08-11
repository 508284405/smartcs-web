package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估A/B测试结果DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalAbTestResultDTO {
    
    /**
     * 基准运行ID
     */
    private String baselineRunId;
    
    /**
     * 实验运行ID
     */
    private String experimentRunId;
    
    /**
     * 置信水平
     */
    private Double confidenceLevel;
    
    /**
     * 测试结果摘要
     */
    private String testSummary;
    
    /**
     * 各指标的比较结果
     */
    private List<MetricComparison> metricComparisons;
    
    /**
     * 统计显著性检验结果
     */
    private Map<String, StatisticalTestResult> statisticalTests;
    
    /**
     * 效应量分析
     */
    private Map<String, EffectSizeAnalysis> effectSizeAnalyses;
    
    /**
     * 功效分析结果
     */
    private PowerAnalysisResult powerAnalysis;
    
    /**
     * 推荐决策
     */
    private String recommendation;
    
    /**
     * 置信区间
     */
    private Map<String, ConfidenceInterval> confidenceIntervals;
    
    /**
     * 指标比较信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetricComparison {
        
        /**
         * 指标名称
         */
        private String metricName;
        
        /**
         * 基准值
         */
        private Double baselineValue;
        
        /**
         * 实验值
         */
        private Double experimentValue;
        
        /**
         * 差异值
         */
        private Double difference;
        
        /**
         * 差异百分比
         */
        private Double differencePercentage;
        
        /**
         * 是否显著
         */
        private Boolean isSignificant;
        
        /**
         * P值
         */
        private Double pValue;
    }
    
    /**
     * 统计检验结果
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatisticalTestResult {
        
        /**
         * 检验类型
         */
        private String testType;
        
        /**
         * 统计量
         */
        private Double statistic;
        
        /**
         * P值
         */
        private Double pValue;
        
        /**
         * 自由度
         */
        private Integer degreesOfFreedom;
        
        /**
         * 是否显著
         */
        private Boolean isSignificant;
        
        /**
         * 临界值
         */
        private Double criticalValue;
    }
    
    /**
     * 效应量分析
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EffectSizeAnalysis {
        
        /**
         * 效应量类型
         */
        private String effectSizeType;
        
        /**
         * 效应量值
         */
        private Double effectSizeValue;
        
        /**
         * 效应量解释
         */
        private String effectSizeInterpretation;
        
        /**
         * 效应量大小分类
         */
        private String effectSizeMagnitude;
    }
    
    /**
     * 功效分析结果
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PowerAnalysisResult {
        
        /**
         * 功效值
         */
        private Double power;
        
        /**
         * 样本大小
         */
        private Integer sampleSize;
        
        /**
         * 最小可检测效应量
         */
        private Double minimumDetectableEffect;
        
        /**
         * 功效是否足够
         */
        private Boolean isPowerSufficient;
    }
    
    /**
     * 置信区间
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfidenceInterval {
        
        /**
         * 下限
         */
        private Double lowerBound;
        
        /**
         * 上限
         */
        private Double upperBound;
        
        /**
         * 置信水平
         */
        private Double confidenceLevel;
    }
}
