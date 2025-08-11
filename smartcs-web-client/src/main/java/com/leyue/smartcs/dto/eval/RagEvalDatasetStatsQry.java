package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG评估数据集统计查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetStatsQry {
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 统计时间范围开始
     */
    private LocalDateTime startTime;
    
    /**
     * 统计时间范围结束
     */
    private LocalDateTime endTime;
    
    /**
     * 统计粒度：hour, day, week, month
     */
    private String timeGranularity;
    
    /**
     * 是否包含使用趋势
     */
    private Boolean includeUsageTrends;
    
    /**
     * 是否包含性能指标
     */
    private Boolean includePerformanceMetrics;
    
    /**
     * 是否包含用户行为分析
     */
    private Boolean includeUserBehavior;
    
    /**
     * 过滤的运行状态列表
     */
    private List<String> runStatuses;
}
