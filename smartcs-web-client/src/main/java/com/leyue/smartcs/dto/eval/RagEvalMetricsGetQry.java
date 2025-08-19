package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估指标查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalMetricsGetQry {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 是否包含详细指标
     */
    private Boolean includeDetailedMetrics;
    
    /**
     * 是否包含历史趋势
     */
    private Boolean includeTrends;
    
    /**
     * 指标类型过滤
     */
    private List<String> metricTypes;
    
    /**
     * 是否包含基准对比
     */
    private Boolean includeBaselineComparison;
    
    /**
     * 基准运行ID
     */
    private String baselineRunId;
}
