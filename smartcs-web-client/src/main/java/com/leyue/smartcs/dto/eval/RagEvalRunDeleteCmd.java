package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估运行删除命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunDeleteCmd {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 是否软删除
     */
    private Boolean softDelete;
    
    /**
     * 是否删除相关的评估结果
     */
    private Boolean deleteResults;
    
    /**
     * 是否删除相关的日志文件
     */
    private Boolean deleteLogs;
    
    /**
     * 删除原因
     */
    private String deleteReason;
}
