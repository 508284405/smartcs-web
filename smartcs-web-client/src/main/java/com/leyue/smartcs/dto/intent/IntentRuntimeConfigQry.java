package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图运行时配置查询
 * 
 * @author Claude
 */
@Data
public class IntentRuntimeConfigQry {
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 区域
     */
    private String region;
    
    /**
     * 环境
     */
    private String env;
    
    /**
     * ETag（用于缓存）
     */
    private String etag;
}