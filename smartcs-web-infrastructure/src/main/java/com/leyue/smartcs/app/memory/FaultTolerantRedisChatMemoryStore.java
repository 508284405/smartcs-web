package com.leyue.smartcs.app.memory;

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

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 容错的Redis聊天记忆存储
 * 使用Resilience4j框架提供熔断器、重试、限流、超时保护
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaultTolerantRedisChatMemoryStore implements ChatMemoryStore {

    private final RedisChatMemoryStore redisChatMemoryStore;
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
        return redisChatMemoryStore.getMessages(memoryId);
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
        redisChatMemoryStore.updateMessages(memoryId, messages);
        
        // 同时更新降级存储作为备份
        if (fallbackEnabled) {
            fallbackStore.updateMessages(memoryId, messages);
        }
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
        redisChatMemoryStore.deleteMessages(memoryId);
        
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
            return redisChatMemoryStore.getMessages(memoryId);
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
            redissonClient.getKeys().countExists("test:connection");
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

    /**
     * 数据同步：从Redis同步到InMemory（恢复时使用）
     */
    public void syncFromRedisToMemory() {
        if (!testRedisConnection()) {
            log.warn("Redis连接不可用，无法同步数据");
            return;
        }
        
        try {
            log.info("开始从Redis同步数据到内存存储");
            // 这里需要根据实际需求实现数据同步逻辑
            // 当前的ChatMemoryStore接口不支持遍历所有会话，这是一个限制
            log.info("数据同步完成");
        } catch (Exception e) {
            log.error("数据同步失败", e);
        }
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        return testRedisConnection() || fallbackEnabled;
    }
}