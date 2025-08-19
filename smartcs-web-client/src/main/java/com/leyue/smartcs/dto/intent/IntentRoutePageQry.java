package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图路由分页查询
 * 
 * @author Claude
 */
@Data
public class IntentRoutePageQry {
    
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
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}