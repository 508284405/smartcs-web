package com.leyue.smartcs.config.async;

import com.leyue.smartcs.config.context.TraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 * 支持MDC上下文传递的异步执行器
 */
@Slf4j
@Configuration
public class AsyncConfig {
    
    /**
     * 自定义异步任务执行器，支持MDC传递
     */
    @Bean("mdcTaskExecutor")
    public Executor mdcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                // 包装任务以支持MDC传递
                super.execute(TraceContextHolder.wrapWithMDC(task));
            }
        };
        
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("MDC-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // 拒绝策略：由调用者线程执行
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("异步任务队列已满，由调用者线程执行任务");
            if (!executor1.isShutdown()) {
                r.run();
            }
        });
        
        executor.initialize();
        log.info("MDC异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}
