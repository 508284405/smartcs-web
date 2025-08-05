package com.leyue.smartcs.rag.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 容错的Redis聊天记忆存储
 * 使用Resilience4j框架提供熔断器、重试、限流、超时保护
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class FaultTolerantRedisChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryStore chatMemoryStore;
    private final RedissonClient redissonClient;
    
    // 降级策略配置
    @Value("${smartcs.ai.memory.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    // 降级存储
    private final InMemoryChatMemoryStore fallbackStore = new InMemoryChatMemoryStore();

    @Override
    @CircuitBreaker(name = "redis-memory-store", fallbackMethod = "getMessagesFallback")
    @Retry(name = "redis-memory-store", fallbackMethod = "getMessagesFallback")
    @Bulkhead(name = "redis-memory-store", fallbackMethod = "getMessagesFallback")
    @TimeLimiter(name = "redis-memory-store", fallbackMethod = "getMessagesFallback")
    public List<ChatMessage> getMessages(Object memoryId) {
        log.debug("从Redis获取消息: memoryId={}", memoryId);
        
        // 检查Redis是否恢复，如果恢复则尝试同步数据
        if (testRedisConnection() && fallbackEnabled) {
            // 这里可以添加Redis恢复时的数据同步逻辑
            // 由于ChatMemoryStore接口限制，暂时只记录日志
            log.debug("Redis连接正常，memoryId={}", memoryId);
        }
        
        return chatMemoryStore.getMessages(memoryId);
    }

    /**
     * 获取消息的降级方法
     */
    public List<ChatMessage> getMessagesFallback(Object memoryId, Exception e) {
        log.warn("Redis获取消息失败，使用内存存储降级: memoryId={}, error={}", memoryId, e.getMessage());
        if (fallbackEnabled) {
            return fallbackStore.getMessages(memoryId);
        } else {
            throw new RuntimeException("Redis存储失败且降级被禁用", e);
        }
    }

    @Override
    @CircuitBreaker(name = "redis-memory-store", fallbackMethod = "updateMessagesFallback")
    @Retry(name = "redis-memory-store", fallbackMethod = "updateMessagesFallback")
    @Bulkhead(name = "redis-memory-store", fallbackMethod = "updateMessagesFallback")
    @TimeLimiter(name = "redis-memory-store", fallbackMethod = "updateMessagesFallback")
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        log.debug("更新Redis消息: memoryId={}", memoryId);
        chatMemoryStore.updateMessages(memoryId, messages);
    }

    /**
     * 更新消息的降级方法
     */
    public void updateMessagesFallback(Object memoryId, List<ChatMessage> messages, Exception e) {
        log.warn("Redis更新消息失败，使用内存存储降级: memoryId={}, error={}", memoryId, e.getMessage());
        if (fallbackEnabled) {
            fallbackStore.updateMessages(memoryId, messages);
        } else {
            throw new RuntimeException("Redis存储失败且降级被禁用", e);
        }
    }

    @Override
    @CircuitBreaker(name = "redis-memory-store", fallbackMethod = "deleteMessagesFallback")
    @Retry(name = "redis-memory-store", fallbackMethod = "deleteMessagesFallback")
    @Bulkhead(name = "redis-memory-store", fallbackMethod = "deleteMessagesFallback")
    @TimeLimiter(name = "redis-memory-store", fallbackMethod = "deleteMessagesFallback")
    public void deleteMessages(Object memoryId) {
        log.debug("删除Redis消息: memoryId={}", memoryId);
        chatMemoryStore.deleteMessages(memoryId);
        
        // 同时删除降级存储中的数据
        if (fallbackEnabled) {
            fallbackStore.deleteMessages(memoryId);
        }
    }

    /**
     * 删除消息的降级方法
     */
    public void deleteMessagesFallback(Object memoryId, Exception e) {
        log.warn("Redis删除消息失败，使用内存存储降级: memoryId={}, error={}", memoryId, e.getMessage());
        if (fallbackEnabled) {
            fallbackStore.deleteMessages(memoryId);
        } else {
            throw new RuntimeException("Redis存储失败且降级被禁用", e);
        }
    }

    /**
     * 异步获取消息（用于TimeLimiter）
     */
    public CompletableFuture<List<ChatMessage>> getMessagesAsync(Object memoryId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("异步从Redis获取消息: memoryId={}", memoryId);
            return chatMemoryStore.getMessages(memoryId);
        });
    }

    /**
     * 异步获取消息的降级方法
     */
    public CompletableFuture<List<ChatMessage>> getMessagesAsyncFallback(Object memoryId, Exception e) {
        log.warn("Redis异步获取消息失败，使用内存存储降级: memoryId={}, error={}", memoryId, e.getMessage());
        if (fallbackEnabled) {
            return CompletableFuture.completedFuture(fallbackStore.getMessages(memoryId));
        } else {
            throw new RuntimeException("Redis存储失败且降级被禁用", e);
        }
    }

    /**
     * 测试Redis连接
     */
    public boolean testRedisConnection() {
        try {
            // 测试基本连接
            redissonClient.getKeys().countExists("test:connection");
            
            // 测试读写操作
            String testKey = "test:connection:" + System.currentTimeMillis();
            redissonClient.getBucket(testKey).set("test", Duration.ofSeconds(5));
            Object result = redissonClient.getBucket(testKey).get();
            redissonClient.getKeys().delete(testKey);
            
            if (result == null || !"test".equals(result.toString())) {
                log.debug("Redis读写测试失败");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.debug("Redis连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取存储状态
     */
    public StorageStatus getStatus() {
        boolean redisAvailable = testRedisConnection();
        
        return new StorageStatus(redisAvailable, 0, 0);
    }

    /**
     * 存储状态信息
     */
    public static class StorageStatus {
        public final boolean redisAvailable;
        public final long failureCount;
        public final long lastFailureTime;
        
        public StorageStatus(boolean redisAvailable, long failureCount, long lastFailureTime) {
            this.redisAvailable = redisAvailable;
            this.failureCount = failureCount;
            this.lastFailureTime = lastFailureTime;
        }
        
        @Override
        public String toString() {
            return String.format("StorageStatus{redisAvailable=%s, failureCount=%d, lastFailureTime=%d}", 
                               redisAvailable, failureCount, lastFailureTime);
        }
    }
}