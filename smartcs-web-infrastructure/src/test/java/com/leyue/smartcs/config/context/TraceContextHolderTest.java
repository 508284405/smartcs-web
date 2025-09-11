package com.leyue.smartcs.config.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.MDC;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceContextHolder单元测试
 * 验证traceId的生成、存储和MDC集成功能
 */
@SpringJUnitConfig
public class TraceContextHolderTest {
    
    private TraceContextHolder traceContextHolder;
    
    @BeforeEach
    void setUp() {
        traceContextHolder = new TraceContextHolder();
        // 确保MDC清空
        MDC.clear();
    }
    
    @AfterEach
    void tearDown() {
        // 测试后清理MDC
        MDC.clear();
    }
    
    @Test
    void testInitTraceContext() {
        // 初始化追踪上下文
        String traceId = traceContextHolder.initTraceContext();
        
        // 验证traceId不为空
        assertNotNull(traceId, "TraceId should not be null");
        assertFalse(traceId.isEmpty(), "TraceId should not be empty");
        
        // 验证traceId格式（应该是32位的十六进制字符串，不包含-）
        assertTrue(traceId.matches("[a-f0-9]{32}"), "TraceId should be 32-character hex string without dashes");
        
        // 验证MDC中存储了traceId
        String mdcTraceId = MDC.get(TraceContextHolder.TRACE_ID_KEY);
        assertEquals(traceId, mdcTraceId, "MDC should contain the same traceId");
    }
    
    @Test
    void testGetCurrentTraceId() {
        // 先初始化
        String originalTraceId = traceContextHolder.initTraceContext();
        
        // 测试获取当前traceId
        String currentTraceId = traceContextHolder.getCurrentTraceId();
        
        assertEquals(originalTraceId, currentTraceId, "Current traceId should match initialized traceId");
    }
    
    @Test
    void testGetCurrentTraceIdWithoutInit() {
        // 不初始化的情况下获取traceId
        String traceId = traceContextHolder.getCurrentTraceId();
        
        // 应该返回null或生成新的traceId
        // 根据实现逻辑，这里应该返回null（因为MDC中没有，SkyWalking也没有）
        assertNull(traceId, "TraceId should be null when not initialized");
    }
    
    @Test
    void testSetTraceId() {
        String customTraceId = "abc123def456789012345678901234567";
        
        // 设置自定义traceId
        traceContextHolder.setTraceId(customTraceId);
        
        // 验证MDC中的traceId
        String mdcTraceId = MDC.get(TraceContextHolder.TRACE_ID_KEY);
        assertEquals(customTraceId, mdcTraceId, "MDC should contain the custom traceId");
        
        // 验证通过getCurrentTraceId能获取到
        String retrievedTraceId = traceContextHolder.getCurrentTraceId();
        assertEquals(customTraceId, retrievedTraceId, "getCurrentTraceId should return custom traceId");
    }
    
    @Test
    void testClearTraceContext() {
        // 初始化追踪上下文
        traceContextHolder.initTraceContext();
        
        // 验证MDC中有traceId
        assertNotNull(MDC.get(TraceContextHolder.TRACE_ID_KEY), "MDC should contain traceId before clear");
        
        // 清理追踪上下文
        traceContextHolder.clearTraceContext();
        
        // 验证MDC中的traceId被清除
        assertNull(MDC.get(TraceContextHolder.TRACE_ID_KEY), "MDC should not contain traceId after clear");
        assertNull(MDC.get(TraceContextHolder.SPAN_ID_KEY), "MDC should not contain spanId after clear");
    }
    
    @Test
    void testGetFullTraceInfo() {
        // 测试没有traceId的情况
        String fullTraceInfo = traceContextHolder.getFullTraceInfo();
        assertEquals("N/A", fullTraceInfo, "Should return N/A when no traceId");
        
        // 初始化traceId
        String traceId = traceContextHolder.initTraceContext();
        
        // 测试只有traceId的情况
        fullTraceInfo = traceContextHolder.getFullTraceInfo();
        assertEquals(traceId, fullTraceInfo, "Should return only traceId when no spanId");
        
        // 设置spanId
        String spanId = "span123456";
        traceContextHolder.setSpanId(spanId);
        
        // 测试有traceId和spanId的情况
        fullTraceInfo = traceContextHolder.getFullTraceInfo();
        assertEquals(traceId + "," + spanId, fullTraceInfo, "Should return traceId,spanId format");
    }
    
    @Test
    void testSkyWalkingAvailability() {
        // 测试SkyWalking可用性检查
        boolean isAvailable = traceContextHolder.isSkyWalkingAvailable();
        
        // 在测试环境中，SkyWalking通常不可用
        assertFalse(isAvailable, "SkyWalking should not be available in test environment");
    }
    
    @Test
    void testMultipleInitCalls() {
        // 第一次初始化
        String firstTraceId = traceContextHolder.initTraceContext();
        
        // 第二次初始化应该返回相同的traceId（从MDC获取）
        String secondTraceId = traceContextHolder.initTraceContext();
        
        assertEquals(firstTraceId, secondTraceId, "Multiple init calls should return the same traceId");
    }
    
    @Test
    void testTraceIdUniqueness() {
        // 创建新的实例进行对比
        TraceContextHolder anotherHolder = new TraceContextHolder();
        
        // 清理MDC确保干净状态
        MDC.clear();
        
        // 生成两个traceId
        String traceId1 = traceContextHolder.initTraceContext();
        
        // 清理MDC模拟新请求
        MDC.clear();
        String traceId2 = anotherHolder.initTraceContext();
        
        // 验证两个traceId不同
        assertNotEquals(traceId1, traceId2, "Different instances should generate different traceIds");
    }
}