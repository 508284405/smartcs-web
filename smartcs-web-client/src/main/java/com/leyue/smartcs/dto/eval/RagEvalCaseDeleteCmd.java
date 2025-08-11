package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估测试用例删除命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseDeleteCmd {
    
    /**
     * 测试用例ID
     */
    private String caseId;
    
    /**
     * 是否软删除
     */
    private Boolean softDelete;
    
    /**
     * 删除原因
     */
    private String deleteReason;
}
