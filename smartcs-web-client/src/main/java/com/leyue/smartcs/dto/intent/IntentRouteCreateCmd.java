package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图路由创建命令
 * 
 * @author Claude
 */
@Data
public class IntentRouteCreateCmd {
    
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
     * 目标服务
     */
    private String targetService;
    
    /**
     * 目标方法
     */
    private String targetMethod;
    
    /**
     * 目标参数
     */
    private String targetParams;
}