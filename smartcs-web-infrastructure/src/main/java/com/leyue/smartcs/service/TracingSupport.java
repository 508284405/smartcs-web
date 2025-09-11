package com.leyue.smartcs.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 追踪上下文传播支持服务
 * 为CompletableFuture和其他异步操作提供追踪上下文传播
 */
@Slf4j
@Service
public class TracingSupport {
    
    /**
     * 包装CompletableFuture的Supplier，确保追踪上下文传播
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return CompletableFuture.supplyAsync(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                return supplier.get();
            } finally {
                MDC.clear();
            }
        });
    }
    
    /**
     * 使用指定执行器的CompletableFuture包装
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return CompletableFuture.supplyAsync(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                return supplier.get();
            } finally {
                MDC.clear();
            }
        }, executor);
    }
    
    /**
     * 包装Runnable以支持追踪上下文传播
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return CompletableFuture.runAsync(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        });
    }
    
    /**
     * 使用指定执行器的Runnable包装
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return CompletableFuture.runAsync(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        }, executor);
    }
    
    /**
     * 包装Callable以支持追踪上下文传播
     */
    public static <T> Callable<T> wrapCallable(Callable<T> callable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }
    
    /**
     * 包装Runnable以支持追踪上下文传播
     */
    public static Runnable wrapRunnable(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
    
    /**
     * 在新线程中执行任务，自动传播追踪上下文
     */
    public static void executeInNewThread(Runnable task, String threadName) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        Thread thread = new Thread(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        });
        
        thread.setName(threadName != null ? threadName : "TracingTask-" + System.currentTimeMillis());
        thread.start();
    }
    
    /**
     * 创建带追踪上下文的线程
     */
    public static Thread createTracingThread(Runnable task, String threadName) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return new Thread(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        }, threadName);
    }
}