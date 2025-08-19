package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估测试用例批量导入命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseBatchImportCmd {
    
    /**
     * 所属数据集ID
     */
    private String datasetId;
    
    /**
     * 测试用例列表
     */
    private List<RagEvalCaseCreateCmd> cases;
    
    /**
     * 导入选项
     */
    private ImportOptions importOptions;
    
    /**
     * 导入选项
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImportOptions {
        /**
         * 是否跳过重复用例
         */
        private Boolean skipDuplicates;
        
        /**
         * 重复检测字段：question, caseId, all
         */
        private String duplicateDetectionField;
        
        /**
         * 是否启用事务
         */
        private Boolean enableTransaction;
        
        /**
         * 批处理大小
         */
        private Integer batchSize;
    }
}
