package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

/**
 * 意图批量分类响应DTO
 * 
 * @author Claude
 */
@Data
public class IntentBatchClassifyResponseDTO {
    
    /**
     * 分类结果列表
     */
    private List<IntentClassifyResponseDTO> results;
    
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
     * 平均置信度
     */
    private Double averageConfidence;
    
    /**
     * 处理时间
     */
    private Long processingTime;
}