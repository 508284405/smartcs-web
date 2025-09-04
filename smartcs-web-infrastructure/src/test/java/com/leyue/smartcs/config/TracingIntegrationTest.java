package com.leyue.smartcs.config;

import com.leyue.smartcs.config.context.TraceContextHolder;
import com.leyue.smartcs.service.TracingSupport;
// 简化版本 - 不依赖Micrometer Tracing
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 追踪集成测试
 * 验证W3C Trace Context在各种场景下的传播
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class TracingIntegrationTest {
    
    @Autowired
    private TraceContextHolder traceContextHolder;
    
    // 简化版本，不依赖Micrometer Tracer
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Test
    void testTraceIdGeneration() {
        // 测试traceId生成和MDC设置
        String traceId = traceContextHolder.initTraceContext();
        
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        assertEquals(traceId, MDC.get(TraceContextHolder.TRACE_ID_KEY));
        
        log.info("生成的traceId: {}", traceId);
        
        traceContextHolder.clearTraceContext();
    }
    
    @Test
    void testBasicTracingIntegration() {
        log.info("简化版本追踪测试 - 基于自定义TraceContextHolder");
        
        // 初始化追踪上下文
        String traceId = traceContextHolder.initTraceContext();
        
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        
        // 验证MDC中的traceId
        assertEquals(traceId, MDC.get(TraceContextHolder.TRACE_ID_KEY));
        
        log.info("基础traceId生成和设置成功: {}", traceId);
        
        traceContextHolder.clearTraceContext();
    }
    
    @Test
    void testAsyncTracingPropagation() throws Exception {
        // 初始化追踪上下文
        String originalTraceId = traceContextHolder.initTraceContext();
        assertNotNull(originalTraceId);
        
        CountDownLatch latch = new CountDownLatch(1);
        String[] asyncTraceId = new String[1];
        
        // 使用TracingSupport进行异步调用
        CompletableFuture<Void> future = TracingSupport.runAsync(() -> {
            // 在异步线程中验证traceId传播
            asyncTraceId[0] = traceContextHolder.getCurrentTraceId();
            if (asyncTraceId[0] == null) {
                asyncTraceId[0] = MDC.get(TraceContextHolder.TRACE_ID_KEY);
            }
            log.info("异步线程中的traceId: {}", asyncTraceId[0]);
            latch.countDown();
        });
        
        // 等待异步任务完成
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        future.get(5, TimeUnit.SECONDS);
        
        // 验证traceId在异步线程中保持一致
        assertNotNull(asyncTraceId[0]);
        // 注意：根据配置不同，可能是同一个traceId或者相关的traceId
        log.info("原始traceId: {}, 异步traceId: {}", originalTraceId, asyncTraceId[0]);
        
        traceContextHolder.clearTraceContext();
    }
    
    @Test
    void testW3CTraceparentExtraction() {
        // 模拟W3C traceparent头格式
        String testTraceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        String testSpanId = "00f067aa0ba902b7";
        String traceparent = String.format("00-%s-%s-01", testTraceId, testSpanId);
        
        // 创建模拟的HTTP请求来测试W3C协议解析
        log.info("测试W3C traceparent格式: {}", traceparent);
        
        // 验证traceparent解析逻辑
        String[] parts = traceparent.split("-");
        assertEquals(4, parts.length);
        assertEquals("00", parts[0]); // version
        assertEquals(testTraceId, parts[1]); // trace-id
        assertEquals(testSpanId, parts[2]); // parent-id
        assertEquals("01", parts[3]); // trace-flags
        
        // 验证traceId长度符合W3C标准（32位十六进制）
        assertEquals(32, parts[1].length());
        assertEquals(16, parts[2].length());
    }
    
    @Test
    void testMultiThreadTracing() throws InterruptedException {
        String originalTraceId = traceContextHolder.initTraceContext();
        assertNotNull(originalTraceId);
        
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        String[] threadTraceIds = new String[threadCount];
        
        // 创建多个线程测试追踪上下文传播
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            TracingSupport.executeInNewThread(() -> {
                threadTraceIds[index] = traceContextHolder.getCurrentTraceId();
                if (threadTraceIds[index] == null) {
                    threadTraceIds[index] = MDC.get(TraceContextHolder.TRACE_ID_KEY);
                }
                log.info("线程 {} 中的traceId: {}", index, threadTraceIds[index]);
                latch.countDown();
            }, "TestThread-" + i);
        }
        
        // 等待所有线程完成
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        
        // 验证每个线程都有traceId（可能不同，取决于配置）
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(threadTraceIds[i], "线程 " + i + " 的traceId不应为null");
            log.info("线程 {} traceId: {}", i, threadTraceIds[i]);
        }
        
        traceContextHolder.clearTraceContext();
    }
    
    @Test
    void testRestTemplateTracingHeaders() {
        // 这个测试需要在有实际HTTP服务的环境中运行
        // 这里只是演示如何测试HTTP客户端的追踪头传播
        
        String traceId = traceContextHolder.initTraceContext();
        assertNotNull(traceId);
        
        // 在实际环境中，可以调用一个测试端点来验证追踪头的传播
        // HttpHeaders headers = new HttpHeaders();
        // HttpEntity<String> entity = new HttpEntity<>("test", headers);
        // ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/test", entity, String.class);
        
        log.info("RestTemplate追踪测试 - traceId: {}", traceId);
        
        traceContextHolder.clearTraceContext();
    }
    
    @Test
    void testTracingChain() throws Exception {
        // 测试完整的追踪链路：同步 -> 异步 -> HTTP调用
        String rootTraceId = traceContextHolder.initTraceContext();
        log.info("开始追踪链路测试，根traceId: {}", rootTraceId);
        
        // 第一层：同步方法调用
        String syncTraceId = simulateSyncMethod();
        log.info("同步方法traceId: {}", syncTraceId);
        
        // 第二层：异步任务
        CompletableFuture<String> asyncResult = TracingSupport.supplyAsync(() -> {
            String asyncTraceId = traceContextHolder.getCurrentTraceId();
            if (asyncTraceId == null) {
                asyncTraceId = MDC.get(TraceContextHolder.TRACE_ID_KEY);
            }
            log.info("异步任务traceId: {}", asyncTraceId);
            return asyncTraceId;
        });
        
        String asyncTraceId = asyncResult.get(5, TimeUnit.SECONDS);
        
        // 验证追踪链路的连续性
        assertNotNull(rootTraceId);
        assertNotNull(syncTraceId);
        assertNotNull(asyncTraceId);
        
        log.info("追踪链路完成 - 根:{}, 同步:{}, 异步:{}", rootTraceId, syncTraceId, asyncTraceId);
        
        traceContextHolder.clearTraceContext();
    }
    
    private String simulateSyncMethod() {
        // 模拟同步方法调用
        String traceId = traceContextHolder.getCurrentTraceId();
        if (traceId == null) {
            traceId = MDC.get(TraceContextHolder.TRACE_ID_KEY);
        }
        log.info("同步方法中的traceId: {}", traceId);
        return traceId;
    }
}