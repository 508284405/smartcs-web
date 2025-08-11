package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RAG评估模型统计DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalModelStatsDTO {
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型版本
     */
    private String modelVersion;
    
    /**
     * 总运行次数
     */
    private Long totalRuns;
    
    /**
     * 成功运行次数
     */
    private Long successfulRuns;
    
    /**
     * 失败运行次数
     */
    private Long failedRuns;
    
    /**
     * 平均延迟（毫秒）
     */
    private Double averageLatency;
    
    /**
     * 平均吞吐量（请求/秒）
     */
    private Double averageThroughput;
    
    /**
     * 成功率
     */
    private Double successRate;
    
    /**
     * 平均指标分数
     */
    private Map<String, Double> averageMetrics;
    
    /**
     * 各状态运行数量分布
     */
    private Map<String, Long> runStatusDistribution;
    
    /**
     * 各数据集运行数量分布
     */
    private Map<String, Long> datasetDistribution;
    
    /**
     * 时间趋势统计
     */
    private List<TimeTrendStat> timeTrendStats;
    
    /**
     * 性能排名
     */
    private List<PerformanceRanking> topPerformances;
    
    /**
     * 成本分析
     */
    private CostAnalysis costAnalysis;
    
    /**
     * 错误分析
     */
    private ErrorAnalysis errorAnalysis;
    
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
         * 平均延迟
         */
        private Double averageLatency;
        
        /**
         * 平均指标分数
         */
        private Map<String, Double> averageMetrics;
        
        /**
         * 平均成本
         */
        private Double averageCost;
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
         * 数据集ID
         */
        private String datasetId;
        
        /**
         * 综合得分
         */
        private Double overallScore;
        
        /**
         * 各维度得分
         */
        private Map<String, Double> dimensionScores;
        
        /**
         * 延迟
         */
        private Double latency;
        
        /**
         * 运行时间
         */
        private LocalDateTime runTime;
    }
    
    /**
     * 成本分析
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CostAnalysis {
        
        /**
         * 总成本
         */
        private Double totalCost;
        
        /**
         * 平均每次运行成本
         */
        private Double averageRunCost;
        
        /**
         * 成本趋势
         */
        private List<CostTrend> costTrends;
        
        /**
         * 成本分布
         */
        private Map<String, Double> costDistribution;
        
        /**
         * 成本优化建议
         */
        private List<String> optimizationSuggestions;
    }
    
    /**
     * 成本趋势
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CostTrend {
        
        /**
         * 时间标签
         */
        private String timeLabel;
        
        /**
         * 成本
         */
        private Double cost;
        
        /**
         * 运行次数
         */
        private Long runCount;
        
        /**
         * 平均成本
         */
        private Double averageCost;
    }
    
    /**
     * 错误分析
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorAnalysis {
        
        /**
         * 总错误次数
         */
        private Long totalErrors;
        
        /**
         * 错误率
         */
        private Double errorRate;
        
        /**
         * 错误类型分布
         */
        private Map<String, Long> errorTypeDistribution;
        
        /**
         * 错误原因分布
         */
        private Map<String, Long> errorReasonDistribution;
        
        /**
         * 常见错误模式
         */
        private List<ErrorPattern> commonErrorPatterns;
        
        /**
         * 错误趋势
         */
        private List<ErrorTrend> errorTrends;
    }
    
    /**
     * 错误模式
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorPattern {
        
        /**
         * 错误类型
         */
        private String errorType;
        
        /**
         * 错误原因
         */
        private String errorReason;
        
        /**
         * 出现次数
         */
        private Long occurrenceCount;
        
        /**
         * 影响范围
         */
        private String impactScope;
        
        /**
         * 解决建议
         */
        private String resolutionSuggestion;
    }
    
    /**
     * 错误趋势
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorTrend {
        
        /**
         * 时间标签
         */
        private String timeLabel;
        
        /**
         * 错误次数
         */
        private Long errorCount;
        
        /**
         * 运行次数
         */
        private Long runCount;
        
        /**
         * 错误率
         */
        private Double errorRate;
    }
}
