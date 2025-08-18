package com.leyue.smartcs.intent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 意图分类配置
 * 
 * @author Claude
 */
@Configuration
@ConfigurationProperties(prefix = "smartcs.intent.classification")
@Data
public class IntentClassificationConfig {
    
    /**
     * 默认模型ID
     */
    private Long defaultModelId = 1L;
    
    /**
     * 默认置信度阈值
     */
    private Double confidenceThreshold = 0.6;
    
    /**
     * 批量分类最大数量
     */
    private Integer maxBatchSize = 100;
    
    /**
     * 分类超时时间（毫秒）
     */
    private Integer timeout = 10000;
    
    /**
     * 是否启用缓存
     */
    private Boolean enableCache = true;
    
    /**
     * 缓存过期时间（秒）
     */
    private Integer cacheExpireSeconds = 300;
    
    /**
     * 困难样本上报开关
     */
    private Boolean enableHardSampleReport = true;
    
    /**
     * 运行时配置刷新间隔（秒）
     */
    private Integer configRefreshInterval = 60;
}