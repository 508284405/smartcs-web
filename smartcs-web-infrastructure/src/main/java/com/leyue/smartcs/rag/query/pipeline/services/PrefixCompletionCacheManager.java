package com.leyue.smartcs.rag.query.pipeline.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 前缀补全缓存管理器
 * 实现多级缓存策略和智能预加载
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrefixCompletionCacheManager {
    
    private final StringRedisTemplate redisTemplate;
    private final PrefixCompletionConfig config;
    
    // L1缓存：本地内存缓存（最热数据）
    private final Map<String, CacheEntry> l1Cache = new ConcurrentHashMap<>();
    
    // L2缓存：Redis（热数据）
    // L3缓存：数据库（所有数据）
    
    // 缓存统计
    private final AtomicLong l1Hits = new AtomicLong(0);
    private final AtomicLong l2Hits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    private static final String REDIS_PREFIX = "pc:cache:";
    private static final String STATS_KEY = "pc:stats";
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final List<String> results;
        private final long timestamp;
        private final long accessCount;
        
        public CacheEntry(List<String> results) {
            this.results = new ArrayList<>(results);
            this.timestamp = System.currentTimeMillis();
            this.accessCount = 1;
        }
        
        private CacheEntry(List<String> results, long timestamp, long accessCount) {
            this.results = results;
            this.timestamp = timestamp;
            this.accessCount = accessCount;
        }
        
        public CacheEntry withAccess() {
            return new CacheEntry(results, System.currentTimeMillis(), accessCount + 1);
        }
        
        public boolean isExpired(long maxAgeMs) {
            return System.currentTimeMillis() - timestamp > maxAgeMs;
        }
        
        public List<String> getResults() { return new ArrayList<>(results); }
        public long getTimestamp() { return timestamp; }
        public long getAccessCount() { return accessCount; }
    }
    
    /**
     * 从缓存获取结果
     */
    public List<String> get(String key) {
        // L1缓存查询
        CacheEntry entry = l1Cache.get(key);
        if (entry != null && !entry.isExpired(config.getCacheExpireMinutes() * 60 * 1000L)) {
            l1Cache.put(key, entry.withAccess()); // 更新访问时间
            l1Hits.incrementAndGet();
            log.debug("L1 cache hit for key: {}", key);
            return entry.getResults();
        }
        
        // L2缓存查询（Redis）
        if (config.isUseRedisCache()) {
            List<String> results = getFromRedis(key);
            if (results != null && !results.isEmpty()) {
                // 提升到L1缓存
                promoteToL1(key, results);
                l2Hits.incrementAndGet();
                log.debug("L2 cache hit for key: {}", key);
                return results;
            }
        }
        
        cacheMisses.incrementAndGet();
        return null;
    }
    
    /**
     * 存储到缓存
     */
    public void put(String key, List<String> results) {
        if (results == null || results.isEmpty()) return;
        
        // 存储到L1缓存（如果空间允许）
        if (l1Cache.size() < config.getMaxCacheSize()) {
            l1Cache.put(key, new CacheEntry(results));
        } else {
            // L1缓存已满，尝试淘汰最少使用的条目
            evictL1Cache();
            if (l1Cache.size() < config.getMaxCacheSize()) {
                l1Cache.put(key, new CacheEntry(results));
            }
        }
        
        // 存储到L2缓存（Redis）
        if (config.isUseRedisCache()) {
            putToRedis(key, results);
        }
    }
    
    /**
     * 预热缓存
     */
    public void warmup(Map<String, List<String>> warmupData) {
        if (warmupData == null || warmupData.isEmpty()) return;
        
        log.info("Starting cache warmup with {} entries", warmupData.size());
        
        int warmedL1 = 0;
        int warmedL2 = 0;
        
        for (Map.Entry<String, List<String>> entry : warmupData.entrySet()) {
            String key = entry.getKey();
            List<String> results = entry.getValue();
            
            try {
                // L1缓存预热
                if (l1Cache.size() < config.getMaxCacheSize()) {
                    l1Cache.put(key, new CacheEntry(results));
                    warmedL1++;
                }
                
                // L2缓存预热
                if (config.isUseRedisCache()) {
                    putToRedis(key, results);
                    warmedL2++;
                }
                
            } catch (Exception e) {
                log.warn("Failed to warmup cache for key: {}", key, e);
            }
        }
        
        log.info("Cache warmup completed. L1: {}, L2: {}", warmedL1, warmedL2);
    }
    
    /**
     * 智能预加载
     * 基于用户行为模式预测可能的查询并预加载
     */
    public void intelligentPreload(List<String> userQueries, 
                                  IPrefixCompletionService completionService) {
        if (userQueries == null || userQueries.isEmpty()) return;
        
        // 分析用户查询模式
        Set<String> predictedPrefixes = predictNextPrefixes(userQueries);
        
        // 异步预加载
        predictedPrefixes.parallelStream().forEach(prefix -> {
            try {
                String cacheKey = buildCacheKey(prefix, null, config.getDefaultLimit());
                if (get(cacheKey) == null) { // 缓存中不存在
                    List<String> results = completionService.complete(prefix, config.getDefaultLimit());
                    if (!results.isEmpty()) {
                        put(cacheKey, results);
                        log.debug("Preloaded cache for prefix: {}", prefix);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to preload cache for prefix: {}", prefix, e);
            }
        });
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanup() {
        long maxAgeMs = config.getCacheExpireMinutes() * 60 * 1000L;
        
        // 清理L1缓存
        l1Cache.entrySet().removeIf(entry -> entry.getValue().isExpired(maxAgeMs));
        
        // 清理Redis缓存中的过期键
        if (config.isUseRedisCache()) {
            cleanupRedisCache();
        }
        
        log.debug("Cache cleanup completed. L1 size: {}", l1Cache.size());
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        long totalRequests = l1Hits.get() + l2Hits.get() + cacheMisses.get();
        double hitRate = totalRequests > 0 ? 
            (double)(l1Hits.get() + l2Hits.get()) / totalRequests : 0.0;
        
        return new CacheStats(
            l1Cache.size(),
            getRedisKeyCount(),
            l1Hits.get(),
            l2Hits.get(),
            cacheMisses.get(),
            hitRate
        );
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        l1Cache.clear();
        
        if (config.isUseRedisCache()) {
            try {
                Set<String> keys = redisTemplate.keys(REDIS_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.warn("Failed to clear Redis cache", e);
            }
        }
        
        log.info("All caches cleared");
    }
    
    // ========== 私有方法 ==========
    
    private List<String> getFromRedis(String key) {
        try {
            String redisKey = REDIS_PREFIX + key;
            List<String> results = redisTemplate.opsForList().range(redisKey, 0, -1);
            return (results != null && !results.isEmpty()) ? results : null;
        } catch (Exception e) {
            log.warn("Failed to get from Redis cache", e);
            return null;
        }
    }
    
    private void putToRedis(String key, List<String> results) {
        try {
            String redisKey = REDIS_PREFIX + key;
            redisTemplate.delete(redisKey); // 先删除旧数据
            redisTemplate.opsForList().rightPushAll(redisKey, results);
            redisTemplate.expire(redisKey, config.getCacheExpireMinutes(), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to put to Redis cache", e);
        }
    }
    
    private void promoteToL1(String key, List<String> results) {
        if (l1Cache.size() < config.getMaxCacheSize()) {
            l1Cache.put(key, new CacheEntry(results));
        } else {
            // 尝试替换访问频率最低的条目
            String leastUsedKey = findLeastUsedKey();
            if (leastUsedKey != null) {
                l1Cache.remove(leastUsedKey);
                l1Cache.put(key, new CacheEntry(results));
            }
        }
    }
    
    private void evictL1Cache() {
        if (l1Cache.isEmpty()) return;
        
        // LRU淘汰策略：移除最少使用的条目
        String evictKey = l1Cache.entrySet().stream()
            .min(Comparator.comparing(entry -> entry.getValue().getAccessCount()))
            .map(Map.Entry::getKey)
            .orElse(null);
            
        if (evictKey != null) {
            l1Cache.remove(evictKey);
            log.debug("Evicted L1 cache entry: {}", evictKey);
        }
    }
    
    private String findLeastUsedKey() {
        return l1Cache.entrySet().stream()
            .min(Comparator.comparing(entry -> entry.getValue().getAccessCount()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private Set<String> predictNextPrefixes(List<String> userQueries) {
        Set<String> prefixes = new HashSet<>();
        
        for (String query : userQueries) {
            if (query == null || query.length() < 2) continue;
            
            query = query.trim().toLowerCase();
            
            // 添加所有可能的前缀
            for (int i = 2; i <= Math.min(query.length(), 6); i++) {
                prefixes.add(query.substring(0, i));
            }
            
            // 基于常见的用户输入模式添加变体
            if (query.length() > 3) {
                // 添加去掉最后一个字符的前缀（模拟用户退格）
                prefixes.add(query.substring(0, query.length() - 1));
            }
        }
        
        return prefixes;
    }
    
    private void cleanupRedisCache() {
        try {
            Set<String> keys = redisTemplate.keys(REDIS_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl == -1) {
                        // 重新设置过期时间
                        redisTemplate.expire(key, config.getCacheExpireMinutes(), TimeUnit.MINUTES);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup Redis cache", e);
        }
    }
    
    private long getRedisKeyCount() {
        try {
            Set<String> keys = redisTemplate.keys(REDIS_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.warn("Failed to get Redis key count", e);
            return 0;
        }
    }
    
    private String buildCacheKey(String prefix, 
                               IPrefixCompletionService.CompletionContext context, 
                               int limit) {
        StringBuilder key = new StringBuilder(prefix).append(":").append(limit);
        if (context != null) {
            if (context.getUserId() != null) key.append(":u:").append(context.getUserId());
            if (context.getDomain() != null) key.append(":d:").append(context.getDomain());
        }
        return key.toString();
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int l1Size;
        private final long l2Size;
        private final long l1Hits;
        private final long l2Hits;
        private final long misses;
        private final double hitRate;
        
        public CacheStats(int l1Size, long l2Size, long l1Hits, long l2Hits, 
                         long misses, double hitRate) {
            this.l1Size = l1Size;
            this.l2Size = l2Size;
            this.l1Hits = l1Hits;
            this.l2Hits = l2Hits;
            this.misses = misses;
            this.hitRate = hitRate;
        }
        
        public int getL1Size() { return l1Size; }
        public long getL2Size() { return l2Size; }
        public long getL1Hits() { return l1Hits; }
        public long getL2Hits() { return l2Hits; }
        public long getMisses() { return misses; }
        public double getHitRate() { return hitRate; }
        
        @Override
        public String toString() {
            return String.format("CacheStats{l1Size=%d, l2Size=%d, l1Hits=%d, l2Hits=%d, misses=%d, hitRate=%.2f%%}",
                l1Size, l2Size, l1Hits, l2Hits, misses, hitRate * 100);
        }
    }
}