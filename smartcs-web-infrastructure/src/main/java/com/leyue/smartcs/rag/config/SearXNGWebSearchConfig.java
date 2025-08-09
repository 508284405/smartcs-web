package com.leyue.smartcs.rag.config;

import dev.langchain4j.community.web.search.searxng.SearXNGWebSearchEngine;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * SearxNG 搜索引擎配置
 * 基于 LangChain4j 1.1.0 + langchain4j-community-web-search-engine-searxng
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SearXNGWebSearchConfig {

    private final SearxngProperties searxngProperties;

    /**
     * 创建 SearxNG 搜索引擎实例
     */
    @Bean
    public SearXNGWebSearchEngine searxngWebSearchEngine() {
        log.info("初始化 SearxNG 搜索引擎，配置: {}", searxngProperties);
        
        // 注意：LangChain4j 1.1.0 版本的 SearXNGWebSearchEngine 仅支持 baseUrl 和 duration 配置
        // 其他配置（userAgent、language、safesearch、maxResults）需要在未来版本或通过其他方式支持
        SearXNGWebSearchEngine engine = SearXNGWebSearchEngine.builder()
                .baseUrl(searxngProperties.getBaseUrl())
                .duration(Duration.ofSeconds(searxngProperties.getTimeout()))
                .build();
                
        // 记录最终生效的参数和预期配置
        log.info("SearxNG 搜索引擎初始化完成 - baseUrl: {}, timeout: {}s", 
                searxngProperties.getBaseUrl(), 
                searxngProperties.getTimeout());
        log.info("预期配置（待未来版本支持）- userAgent: {}, language: {}, safeContent: {}, maxResults: {}",
                searxngProperties.getUserAgent(),
                searxngProperties.getResultLanguage(),
                searxngProperties.isSafeContent(),
                searxngProperties.getMaxResults());
                
        return engine;
    }

    /**
     * SearxNG 配置属性
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "smartcs.ai.web-search.searxng")
    public static class SearxngProperties {
        /**
         * SearxNG 实例基础URL
         */
        private String baseUrl = "https://searx.be";

        /**
         * 连接超时时间（秒）
         */
        private int timeout = 10;

        /**
         * 最大返回结果数
         */
        private int maxResults = 8;

        /**
         * 用户代理
         */
        private String userAgent = "SmartCS-Web/1.0.0";

        /**
         * 结果语言
         */
        private String resultLanguage = "zh-CN";

        /**
         * 是否过滤成人内容
         */
        private boolean safeContent = true;
    }
} 