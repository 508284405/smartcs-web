package com.leyue.smartcs.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(name = "commonThreadPoolExecutor")
    public Executor commonThreadPoolExecutor() {
        int corePoolSize = 8;
        int maxPoolSize = 32;
        long keepAliveTime = 60L;
        int queueCapacity = 200;
        ThreadFactory threadFactory = r -> {
            Thread t = java.util.concurrent.Executors.defaultThreadFactory().newThread(r);
            t.setName("common-exec-" + t.getId());
            return t;
        };
        return TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        ));
    }
}
