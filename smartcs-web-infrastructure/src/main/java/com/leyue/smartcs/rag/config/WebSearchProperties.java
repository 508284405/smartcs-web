package com.leyue.smartcs.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Web搜索配置属性
 * 用于控制Web搜索功能的全局开关和相关参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "smartcs.ai.web-search")
public class WebSearchProperties {

    /**
     * Web搜索功能全局开关
     * 默认为false，避免上游服务异常影响整体功能
     */
    private boolean enabled = false;
}