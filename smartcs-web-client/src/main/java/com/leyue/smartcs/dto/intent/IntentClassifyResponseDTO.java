package com.leyue.smartcs.dto.intent;

import lombok.Data;

import java.util.Map;

/**
 * 意图分类响应DTO
 * 
 * @author Claude
 */
@Data
public class IntentClassifyResponseDTO {
    
    /**
     * 识别的意图编码
     */
    private String intentCode;
    
    /**
     * 意图名称
     */
    private String intentName;
    
    /**
     * 置信度分数
     */
    private Double confidenceScore;
    
    /**
     * 是否超过阈值
     */
    private Boolean aboveThreshold;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 快照ID
     */
    private String snapshotId;
    
    /**
     * 分类时间
     */
    private Long classificationTime;
    
    /**
     * 处理时间（毫秒）
     */
    private Integer processingTimeMs;
    
    /**
     * 完整结果数据
     */
    private Map<String, Object> resultData;
}