package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估比较结果DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCompareResultDTO {
    
    /**
     * 要比较的运行ID列表
     */
    private List<String> runIds;
    
    /**
     * 比较维度列表
     */
    private List<String> comparisonDimensions;
    
    /**
     * 比较结果摘要
     */
    private String comparisonSummary;
    
    /**
     * 各运行的关键指标
     */
    private Map<String, Map<String, Double>> keyMetrics;
    
    /**
     * 性能排名
     */
    private List<PerformanceRanking> performanceRankings;
    
    /**
     * 差异分析
     */
    private List<DifferenceAnalysis> differenceAnalyses;
    
    /**
     * 统计显著性检验结果
     */
    private Map<String, StatisticalTestResult> statisticalTests;
    
    /**
     * 可视化图表数据
     */
    private Map<String, Object> visualizationData;
    
    /**
     * 比较完成时间
     */
    private Long comparisonTime;
    
    /**
     * 性能排名信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceRanking {
        
        /**
         * 运行ID
         */
        private String runId;
        
        /**
         * 排名
         */
        private Integer rank;
        
        /**
         * 综合得分
         */
        private Double overallScore;
        
        /**
         * 各维度得分
         */
        private Map<String, Double> dimensionScores;
    }
    
    /**
     * 差异分析信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DifferenceAnalysis {
        
        /**
         * 比较维度
         */
        private String dimension;
        
        /**
         * 基准运行ID
         */
        private String baselineRunId;
        
        /**
         * 对比运行ID
         */
        private String comparisonRunId;
        
        /**
         * 差异值
         */
        private Double difference;
        
        /**
         * 差异百分比
         */
        private Double differencePercentage;
        
        /**
         * 差异显著性
         */
        private String significance;
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
         * P值
         */
        private Double pValue;
        
        /**
         * 统计量
         */
        private Double statistic;
        
        /**
         * 自由度
         */
        private Integer degreesOfFreedom;
        
        /**
         * 是否显著
         */
        private Boolean isSignificant;
        
        /**
         * 效应量
         */
        private Double effectSize;
    }
}
