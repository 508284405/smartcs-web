package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG评估模型统计查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalModelStatsQry {
    
    /**
     * 模型ID
     */
    private Long modelId;
    
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
     * 是否包含性能趋势
     */
    private Boolean includePerformanceTrends;
    
    /**
     * 是否包含成本分析
     */
    private Boolean includeCostAnalysis;
    
    /**
     * 是否包含错误分析
     */
    private Boolean includeErrorAnalysis;
    
    /**
     * 是否包含延迟分析
     */
    private Boolean includeLatencyAnalysis;
    
    /**
     * 过滤的数据集ID列表
     */
    private List<String> datasetIds;
    
    /**
     * 过滤的运行状态列表
     */
    private List<String> runStatuses;
}
