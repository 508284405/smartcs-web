package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估统计概览DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalStatisticsDTO {
    
    /**
     * 总数据集数量
     */
    private Long totalDatasets;
    
    /**
     * 总运行数量
     */
    private Long totalRuns;
    
    /**
     * 总测试用例数量
     */
    private Long totalCases;
    
    /**
     * 活跃数据集数量
     */
    private Long activeDatasets;
    
    /**
     * 活跃运行数量
     */
    private Long activeRuns;
    
    /**
     * 成功运行数量
     */
    private Long successfulRuns;
    
    /**
     * 失败运行数量
     */
    private Long failedRuns;
    
    /**
     * 平均运行时长（分钟）
     */
    private Double averageRunDuration;
    
    /**
     * 平均指标分数
     */
    private Map<String, Double> averageMetrics;
    
    /**
     * 各状态运行数量分布
     */
    private Map<String, Long> runStatusDistribution;
    
    /**
     * 各类型运行数量分布
     */
    private Map<String, Long> runTypeDistribution;
    
    /**
     * 各领域数据集数量分布
     */
    private Map<String, Long> domainDistribution;
    
    /**
     * 各难度测试用例数量分布
     */
    private Map<String, Long> difficultyDistribution;
    
    /**
     * 时间趋势统计
     */
    private List<TimeTrendStat> timeTrendStats;
    
    /**
     * 性能排名
     */
    private List<PerformanceRanking> topPerformers;
    
    /**
     * 时间趋势统计
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeTrendStat {
        
        /**
         * 时间标签
         */
        private String timeLabel;
        
        /**
         * 运行数量
         */
        private Long runCount;
        
        /**
         * 成功数量
         */
        private Long successCount;
        
        /**
         * 平均指标分数
         */
        private Map<String, Double> averageMetrics;
    }
    
    /**
     * 性能排名
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceRanking {
        
        /**
         * 排名
         */
        private Integer rank;
        
        /**
         * 实体ID（数据集ID或模型ID）
         */
        private String entityId;
        
        /**
         * 实体名称
         */
        private String entityName;
        
        /**
         * 实体类型
         */
        private String entityType;
        
        /**
         * 综合得分
         */
        private Double overallScore;
        
        /**
         * 各维度得分
         */
        private Map<String, Double> dimensionScores;
        
        /**
         * 运行次数
         */
        private Long runCount;
    }
}
