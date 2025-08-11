package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估比较查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCompareQry {
    
    /**
     * 要比较的运行ID列表
     */
    private List<String> runIds;
    
    /**
     * 比较维度列表
     */
    private List<String> comparisonDimensions;
    
    /**
     * 是否包含统计显著性检验
     */
    private Boolean includeStatisticalTests;
    
    /**
     * 是否包含可视化图表
     */
    private Boolean includeVisualizations;
    
    /**
     * 基准运行ID（用于相对比较）
     */
    private String baselineRunId;
    
    /**
     * 比较结果的详细程度：basic, detailed, comprehensive
     */
    private String detailLevel;
}
