package com.leyue.smartcs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 异步追踪服务
 * 用于测试Spring异步方法中的traceId传递
 */
@Slf4j
@Service
public class AsyncTraceService {
    
    /**
     * 异步处理任务，测试traceId传递
     */
    @Async("mdcTaskExecutor")
    public CompletableFuture<String> processAsyncTask(String taskName) {
        log.info("开始执行异步任务: {}", taskName);
        
        try {
            // 模拟处理时间
            Thread.sleep(200);
            
            log.debug("异步任务 {} 处理中...", taskName);
            
            // 模拟一些业务逻辑
            if (taskName.contains("error")) {
                log.error("异步任务 {} 遇到错误", taskName);
                throw new RuntimeException("模拟异步任务错误");
            }
            
            log.info("异步任务 {} 执行完成", taskName);
            return CompletableFuture.completedFuture("Task " + taskName + " completed");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("异步任务 {} 被中断", taskName, e);
            return CompletableFuture.completedFuture("Task " + taskName + " interrupted");
        } catch (Exception e) {
            log.error("异步任务 {} 执行失败", taskName, e);
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
    
    /**
     * 同步方法，用于对比测试
     */
    public String processSyncTask(String taskName) {
        log.info("开始执行同步任务: {}", taskName);
        
        try {
            Thread.sleep(100);
            log.debug("同步任务 {} 处理中...", taskName);
            log.info("同步任务 {} 执行完成", taskName);
            return "Sync Task " + taskName + " completed";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("同步任务 {} 被中断", taskName, e);
            return "Sync Task " + taskName + " interrupted";
        }
    }
}