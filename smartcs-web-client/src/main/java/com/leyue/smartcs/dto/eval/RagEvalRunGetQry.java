package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估运行查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunGetQry {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 是否包含详细信息
     */
    private Boolean includeDetails;
    
    /**
     * 是否包含评估结果
     */
    private Boolean includeResults;
    
    /**
     * 是否包含性能指标
     */
    private Boolean includeMetrics;
}
