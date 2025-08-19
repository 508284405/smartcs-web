package com.leyue.smartcs.dto.intent;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 意图版本DTO
 * 
 * @author Claude
 */
@Data
public class IntentVersionDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 意图名称
     */
    private String intentName;
    
    /**
     * 版本号
     */
    private String versionNumber;
    
    /**
     * 版本名称
     */
    private String versionName;
    
    /**
     * 配置快照
     */
    private Map<String, Object> configSnapshot;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 样本数量
     */
    private Integer sampleCount;
    
    /**
     * 准确率分数
     */
    private BigDecimal accuracyScore;
    
    /**
     * 变更说明
     */
    private String changeNote;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 审批者ID
     */
    private Long approvedBy;
    
    /**
     * 审批时间
     */
    private Long approvedAt;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}