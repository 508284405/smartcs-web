package com.leyue.smartcs.web.test;

import com.leyue.smartcs.config.context.TraceContextHolder;
import com.leyue.smartcs.service.AsyncTraceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 追踪功能测试控制器
 * 用于验证traceId在日志中的正确显示
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TraceTestController {
    
    private final TraceContextHolder traceContextHolder;
    private final AsyncTraceService asyncTraceService;
    
    /**
     * 测试traceId日志输出
     * 访问此接口可以验证日志中是否正确显示traceId
     */
    @GetMapping("/trace")
    public String testTrace() {
        String traceId = traceContextHolder.getCurrentTraceId();
        
        log.info("开始处理追踪测试请求");
        log.debug("当前traceId: {}", traceId);
        log.warn("这是一条警告日志");
        log.error("这是一条错误日志");
        
        // 模拟一些业务处理
        simulateBusinessLogic();
        
        log.info("追踪测试请求处理完成");
        
        return "TraceId: " + traceId + 
               "\nSkyWalking可用: " + traceContextHolder.isSkyWalkingAvailable() +
               "\n完整追踪信息: " + traceContextHolder.getFullTraceInfo();
    }
    
    /**
     * 测试子线程traceId传递
     */
    @GetMapping("/trace/async")
    public String testAsyncTrace() {
        String traceId = traceContextHolder.getCurrentTraceId();
        
        log.info("主线程开始处理异步追踪测试");
        
        // 测试1: 使用包装的Runnable
        Thread thread1 = new Thread(TraceContextHolder.wrapWithMDC(() -> {
            log.info("子线程1执行任务");
            try {
                Thread.sleep(100);
                log.debug("子线程1详细处理");
            } catch (InterruptedException e) {
                log.error("子线程1被中断", e);
            }
            log.info("子线程1任务完成");
        }));
        thread1.setName("AsyncTask-1");
        thread1.start();
        
        // 测试2: 使用TraceContextHolder提供的便捷方法
        traceContextHolder.executeInNewThread(() -> {
            log.info("子线程2执行任务");
            try {
                Thread.sleep(150);
                log.debug("子线程2详细处理");
            } catch (InterruptedException e) {
                log.error("子线程2被中断", e);
            }
            log.info("子线程2任务完成");
        });
        
        // 等待子线程完成（仅用于测试）
        try {
            thread1.join();
            Thread.sleep(200); // 等待第二个线程完成
        } catch (InterruptedException e) {
            log.error("主线程等待被中断", e);
        }
        
        log.info("异步追踪测试完成");
        
        return "异步TraceId测试完成: " + traceId;
    }
    
    /**
     * 测试Spring异步方法中的traceId传递
     */
    @GetMapping("/trace/spring-async")
    public String testSpringAsyncTrace() {
        String traceId = traceContextHolder.getCurrentTraceId();
        
        log.info("开始Spring异步追踪测试");
        
        try {
            // 启动多个异步任务
            CompletableFuture<String> task1 = asyncTraceService.processAsyncTask("task-1");
            CompletableFuture<String> task2 = asyncTraceService.processAsyncTask("task-2");
            
            // 执行同步任务进行对比
            String syncResult = asyncTraceService.processSyncTask("sync-task");
            log.info("同步任务结果: {}", syncResult);
            
            // 等待异步任务完成
            String result1 = task1.get();
            String result2 = task2.get();
            
            log.info("异步任务1结果: {}", result1);
            log.info("异步任务2结果: {}", result2);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Spring异步任务执行失败", e);
        }
        
        log.info("Spring异步追踪测试完成");
        
        return "Spring异步TraceId测试完成: " + traceId;
    }
    
    /**
     * 模拟业务逻辑处理
     */
    private void simulateBusinessLogic() {
        log.info("执行业务逻辑步骤1");
        
        try {
            // 模拟一些处理时间
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("线程被中断", e);
        }
        
        log.info("执行业务逻辑步骤2");
        log.debug("业务处理详细信息");
        
        // 模拟可能的异常情况
        if (Math.random() < 0.1) {
            log.warn("发生了一个需要注意的情况");
        }
        
        log.info("业务逻辑处理完成");
    }
}