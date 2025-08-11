package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RAG评估数据集统计DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetStatsDTO {
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 数据集名称
     */
    private String datasetName;
    
    /**
     * 总运行次数
     */
    private Long totalRuns;
    
    /**
     * 总测试用例数量
     */
    private Long totalCases;
    
    /**
     * 成功运行次数
     */
    private Long successfulRuns;
    
    /**
     * 失败运行次数
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
     * 各难度测试用例数量分布
     */
    private Map<String, Long> caseDifficultyDistribution;
    
    /**
     * 各类别测试用例数量分布
     */
    private Map<String, Long> caseCategoryDistribution;
    
    /**
     * 时间趋势统计
     */
    private List<TimeTrendStat> timeTrendStats;
    
    /**
     * 性能排名
     */
    private List<PerformanceRanking> topPerformers;
    
    /**
     * 使用频率统计
     */
    private UsageFrequencyStats usageFrequencyStats;
    
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
        
        /**
         * 活跃用户数量
         */
        private Long activeUsers;
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
         * 运行ID
         */
        private String runId;
        
        /**
         * 运行名称
         */
        private String runName;
        
        /**
         * 模型ID
         */
        private Long modelId;
        
        /**
         * 综合得分
         */
        private Double overallScore;
        
        /**
         * 各维度得分
         */
        private Map<String, Double> dimensionScores;
        
        /**
         * 运行时间
         */
        private LocalDateTime runTime;
    }
    
    /**
     * 使用频率统计
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UsageFrequencyStats {
        
        /**
         * 日使用频率
         */
        private Map<String, Long> dailyUsage;
        
        /**
         * 周使用频率
         */
        private Map<String, Long> weeklyUsage;
        
        /**
         * 月使用频率
         */
        private Map<String, Long> monthlyUsage;
        
        /**
         * 最活跃时间段
         */
        private String peakUsageTime;
        
        /**
         * 平均每日使用次数
         */
        private Double averageDailyUsage;
    }
}
