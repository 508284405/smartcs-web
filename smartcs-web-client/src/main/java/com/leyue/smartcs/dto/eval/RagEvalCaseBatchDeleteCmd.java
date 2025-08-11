package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估测试用例批量删除命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseBatchDeleteCmd {
    
    /**
     * 测试用例ID列表
     */
    private List<String> caseIds;
    
    /**
     * 是否软删除
     */
    private Boolean softDelete;
    
    /**
     * 删除原因
     */
    private String deleteReason;
    
    /**
     * 是否启用事务
     */
    private Boolean enableTransaction;
}
