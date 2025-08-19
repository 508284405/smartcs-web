package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估运行状态查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunStatusQry {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 是否包含进度信息
     */
    private Boolean includeProgress;
    
    /**
     * 是否包含错误详情
     */
    private Boolean includeErrorDetails;
}
