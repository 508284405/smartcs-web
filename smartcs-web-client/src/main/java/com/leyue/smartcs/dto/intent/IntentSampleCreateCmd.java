package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图样本创建命令
 * 
 * @author Claude
 */
@Data
public class IntentSampleCreateCmd {
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 文本内容
     */
    private String text;
    
    /**
     * 样本类型
     */
    private String type;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 租户
     */
    private String tenant;
}