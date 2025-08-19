package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图样本批量导入结果DTO
 * 
 * @author Claude
 */
@Data
public class IntentSampleBatchImportResultDTO {
    
    /**
     * 总数
     */
    private Integer totalCount;
    
    /**
     * 成功数
     */
    private Integer successCount;
    
    /**
     * 失败数
     */
    private Integer failureCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}