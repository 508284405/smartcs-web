package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估数据集删除命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetDeleteCmd {
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 是否软删除
     */
    private Boolean softDelete;
    
    /**
     * 是否删除相关的测试用例
     */
    private Boolean deleteCases;
    
    /**
     * 是否删除相关的运行记录
     */
    private Boolean deleteRuns;
    
    /**
     * 是否删除相关的评估结果
     */
    private Boolean deleteResults;
    
    /**
     * 删除原因
     */
    private String deleteReason;
    
    /**
     * 是否启用事务
     */
    private Boolean enableTransaction;
    
    /**
     * 是否备份数据
     */
    private Boolean backupData;
    
    /**
     * 备份路径
     */
    private String backupPath;
}
