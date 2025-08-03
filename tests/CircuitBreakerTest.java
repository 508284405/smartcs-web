package com.leyue.smartcs;

import com.leyue.smartcs.app.memory.FaultTolerantRedisChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 熔断器功能测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class CircuitBreakerTest {

    @Autowired
    private FaultTolerantRedisChatMemoryStore memoryStore;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    public void testCircuitBreakerConfiguration() {
        // 验证熔断器配置是否正确加载
        CircuitBreaker redisCircuitBreaker = circuitBreakerRegistry.circuitBreaker("redis-memory-store");
        assertNotNull(redisCircuitBreaker);
        
        CircuitBreaker.State initialState = redisCircuitBreaker.getState();
        assertEquals(CircuitBreaker.State.CLOSED, initialState);
    }

    @Test
    public void testMemoryStoreWithCircuitBreaker() {
        // 测试正常的消息存储和获取
        String memoryId = "test-session-" + System.currentTimeMillis();
        List<ChatMessage> messages = Arrays.asList(
            new UserMessage("Hello, how are you?"),
            new UserMessage("I'm doing well, thank you!")
        );

        // 存储消息
        memoryStore.updateMessages(memoryId, messages);
        
        // 获取消息
        List<ChatMessage> retrievedMessages = memoryStore.getMessages(memoryId);
        assertNotNull(retrievedMessages);
        assertEquals(2, retrievedMessages.size());
    }

    @Test
    public void testCircuitBreakerFallback() {
        // 测试降级功能
        String memoryId = "fallback-test-" + System.currentTimeMillis();
        List<ChatMessage> messages = Arrays.asList(
            new UserMessage("Test message for fallback")
        );

        // 即使Redis不可用，也应该能够通过降级机制工作
        memoryStore.updateMessages(memoryId, messages);
        List<ChatMessage> retrievedMessages = memoryStore.getMessages(memoryId);
        
        assertNotNull(retrievedMessages);
        assertFalse(retrievedMessages.isEmpty());
    }

    @Test
    public void testCircuitBreakerMetrics() {
        // 测试熔断器指标收集
        CircuitBreaker redisCircuitBreaker = circuitBreakerRegistry.circuitBreaker("redis-memory-store");
        
        // 执行一些操作来生成指标
        String memoryId = "metrics-test-" + System.currentTimeMillis();
        List<ChatMessage> messages = Arrays.asList(
            new UserMessage("Test message for metrics")
        );

        memoryStore.updateMessages(memoryId, messages);
        memoryStore.getMessages(memoryId);

        // 验证指标是否被正确收集
        CircuitBreaker.Metrics metrics = redisCircuitBreaker.getMetrics();
        assertNotNull(metrics);
        
        // 验证失败率指标
        float failureRate = metrics.getFailureRate();
        assertTrue(failureRate >= 0.0f);
        assertTrue(failureRate <= 100.0f);
    }

    @Test
    public void testCircuitBreakerStateTransitions() {
        // 测试熔断器状态转换
        CircuitBreaker redisCircuitBreaker = circuitBreakerRegistry.circuitBreaker("redis-memory-store");
        
        // 初始状态应该是CLOSED
        assertEquals(CircuitBreaker.State.CLOSED, redisCircuitBreaker.getState());
        
        // 手动重置熔断器状态
        redisCircuitBreaker.reset();
        assertEquals(CircuitBreaker.State.CLOSED, redisCircuitBreaker.getState());
    }

    @Test
    public void testHealthCheck() {
        // 测试健康检查功能
        assertTrue(memoryStore.isHealthy());
        
        // 测试存储状态
        FaultTolerantRedisChatMemoryStore.StorageStatus status = memoryStore.getStatus();
        assertNotNull(status);
        assertNotNull(status.toString());
    }
} 