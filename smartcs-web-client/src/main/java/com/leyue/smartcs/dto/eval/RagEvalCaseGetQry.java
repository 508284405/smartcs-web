package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估测试用例查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseGetQry {
    
    /**
     * 测试用例ID
     */
    private String caseId;
    
    /**
     * 是否包含详细信息
     */
    private Boolean includeDetails;
    
    /**
     * 是否包含评估历史
     */
    private Boolean includeEvaluationHistory;
}
