package com.leyue.smartcs.dto.intent;

import lombok.Data;

import java.util.Map;

/**
 * 意图运行时配置DTO
 * 
 * @author Claude
 */
@Data
public class IntentRuntimeConfigDTO {
    
    /**
     * 快照ID
     */
    private String snapshotId;
    
    /**
     * ETag
     */
    private String etag;
    
    /**
     * 生成时间
     */
    private Long generatedAt;
    
    /**
     * 作用域
     */
    private String scope;
    
    /**
     * 作用域选择器
     */
    private Map<String, Object> scopeSelector;
    
    /**
     * 意图配置
     */
    private Map<String, Object> intents;
    
    /**
     * 请求上下文
     */
    private Map<String, Object> requestContext;
    
    // 扩展字段用于服务实现
    
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
     * 配置版本
     */
    private String configVersion;
    
    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;
    
    /**
     * 默认阈值
     */
    private Double defaultThreshold;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    /**
     * 超时时间
     */
    private Integer timeout;
    
    /**
     * 意图阈值配置
     */
    private Map<String, Object> intentThresholds;
}