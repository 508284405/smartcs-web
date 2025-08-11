package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估A/B测试查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalAbTestQry {
    
    /**
     * 基准运行ID
     */
    private String baselineRunId;
    
    /**
     * 实验运行ID
     */
    private String experimentRunId;
    
    /**
     * 置信水平（默认0.95）
     */
    private Double confidenceLevel;
    
    /**
     * 是否包含效应量计算
     */
    private Boolean includeEffectSize;
    
    /**
     * 是否包含功效分析
     */
    private Boolean includePowerAnalysis;
    
    /**
     * 关注的指标列表
     */
    private List<String> focusMetrics;
    
    /**
     * 是否包含详细的统计检验结果
     */
    private Boolean includeDetailedStats;
}
