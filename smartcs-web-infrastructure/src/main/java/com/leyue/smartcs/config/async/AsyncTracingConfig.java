package com.leyue.smartcs.config.async;

import com.leyue.smartcs.config.context.TraceContextHolder;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 异步任务追踪配置
 * 确保在异步执行中正确传播追踪上下文
 */
@Configuration
@EnableAsync
public class AsyncTracingConfig implements AsyncConfigurer {
    
    /**
     * 配置异步任务执行器，支持追踪上下文传播
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("SmartCS-Async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // 配置TaskDecorator以传播追踪上下文
        executor.setTaskDecorator(tracingTaskDecorator());
        executor.initialize();
        
        return executor;
    }
    
    /**
     * 追踪上下文传播装饰器
     * 使用MDC传播追踪上下文
     */
    @Bean
    public TaskDecorator tracingTaskDecorator() {
        return runnable -> {
            // 捕获当前线程的MDC上下文
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            
            return () -> {
                // 在子线程中恢复上下文
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    runnable.run();
                } finally {
                    // 清理子线程的MDC上下文
                    MDC.clear();
                }
            };
        };
    }
    
    /**
     * 为手动创建的线程池提供追踪装饰器
     */
    @Bean("tracingThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor tracingThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("SmartCS-Tracing-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // 配置追踪上下文传播
        executor.setTaskDecorator(tracingTaskDecorator());
        executor.initialize();
        
        return executor;
    }
}