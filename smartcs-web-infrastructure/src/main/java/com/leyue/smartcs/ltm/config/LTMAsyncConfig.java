package com.leyue.smartcs.ltm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * LTM异步执行器配置
 * 提供名称为 "ltmTaskExecutor" 的线程池，供记忆巩固等@Async任务使用。
 */
@Slf4j
@Configuration
public class LTMAsyncConfig {

    @Value("${smartcs.ai.ltm.performance.async-executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${smartcs.ai.ltm.performance.async-executor.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${smartcs.ai.ltm.performance.async-executor.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${smartcs.ai.ltm.performance.async-executor.thread-name-prefix:ltm-async-}")
    private String threadNamePrefix;

    @Bean("ltmTaskExecutor")
    public ThreadPoolTaskExecutor ltmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("ltmTaskExecutor initialized: core={}, max={}, queueCapacity={}", corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }
}

