package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG评估统计查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalStatisticsQry {
    
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
     * 统计粒度：hour, day, week, month, quarter, year
     */
    private String timeGranularity;
    
    /**
     * 是否包含详细统计
     */
    private Boolean includeDetailedStats;
    
    /**
     * 是否包含趋势分析
     */
    private Boolean includeTrendAnalysis;
    
    /**
     * 是否包含性能排名
     */
    private Boolean includePerformanceRanking;
    
    /**
     * 排名数量限制
     */
    private Integer rankingLimit;
    
    /**
     * 过滤的运行状态
     */
    private List<String> runStatuses;
    
    /**
     * 过滤的运行类型
     */
    private List<String> runTypes;
    
    /**
     * 是否包含成本分析
     */
    private Boolean includeCostAnalysis;
    
    /**
     * 是否包含错误分析
     */
    private Boolean includeErrorAnalysis;
}
