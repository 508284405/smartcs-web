package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图策略创建命令
 * 
 * @author Claude
 */
@Data
public class IntentPolicyCreateCmd {
    
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
}