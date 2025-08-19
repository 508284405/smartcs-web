package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图策略更新命令
 * 
 * @author Claude
 */
@Data
public class IntentPolicyUpdateCmd {
    
    /**
     * 策略ID
     */
    private Long policyId;
    
    /**
     * 阈值
     */
    private Double threshold;
    
    /**
     * 描述
     */
    private String description;
}