package com.leyue.smartcs.eval.client;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 简化评估服务客户端配置
 * 配置超时、重试和日志
 */
@Configuration
public class SimpleEvalClientConfig {
    
    @Value("${eval.simple-eval.timeout-ms:120000}")
    private int timeoutMs;
    
    @Value("${eval.simple-eval.max-retries:3}")
    private int maxRetries;
    
    @Value("${eval.simple-eval.retry-interval-ms:2000}")
    private int retryIntervalMs;
    
    /**
     * 配置请求超时
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
                timeoutMs / 10, TimeUnit.MILLISECONDS, // 连接超时
                timeoutMs, TimeUnit.MILLISECONDS,      // 读取超时
                true                                   // 跟随重定向
        );
    }
    
    /**
     * 配置重试策略
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                retryIntervalMs,           // 初始间隔
                retryIntervalMs * 2,       // 最大间隔
                maxRetries                 // 最大重试次数
        );
    }
    
    /**
     * 配置日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}