package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估测试用例批量导入结果DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseBatchImportResultDTO {
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 失败的测试用例列表
     */
    private List<FailedCase> failedCases;
    
    /**
     * 导入耗时（毫秒）
     */
    private Long importTime;
    
    /**
     * 导入状态：SUCCESS, PARTIAL_SUCCESS, FAILED
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 失败的测试用例信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailedCase {
        
        /**
         * 原始数据索引
         */
        private Integer index;
        
        /**
         * 失败原因
         */
        private String reason;
        
        /**
         * 原始数据
         */
        private String originalData;
    }
}
