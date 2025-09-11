package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图策略DTO
 * 
 * @author Claude
 */
@Data
public class IntentPolicyDTO {
    
    /**
     * 策略ID
     */
    private Long id;
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 阈值
     */
    private Double threshold;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}