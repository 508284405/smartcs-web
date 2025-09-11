package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图分类命令
 * 
 * @author Claude
 */
@Data
public class IntentClassifyCmd {
    
    /**
     * 待分类文本
     */
    private String text;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 区域
     */
    private String region;
    
    /**
     * 环境
     */
    private String env;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private Long userId;
}