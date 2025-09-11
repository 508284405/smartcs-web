package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图路由更新命令
 * 
 * @author Claude
 */
@Data
public class IntentRouteUpdateCmd {
    
    /**
     * 路由ID
     */
    private Long routeId;
    
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