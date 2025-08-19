package com.leyue.smartcs.intent.service;

import com.leyue.smartcs.dto.intent.IntentRuntimeConfigDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 意图运行时配置缓存服务
 * 负责配置的缓存管理和同步
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntentRuntimeConfigCacheService {
    
    private final RedissonClient redissonClient;
    
    // 本地缓存，提高访问性能
    private final ConcurrentHashMap<String, IntentRuntimeConfigDTO> localCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    private static final String CACHE_PREFIX = "intent:runtime:config:";
    private static final String ETAG_PREFIX = "intent:runtime:etag:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private static final long LOCAL_CACHE_TTL = 5 * 60 * 1000; // 5分钟本地缓存
    
    /**
     * 获取运行时配置
     * 
     * @param channel 渠道
     * @param tenant 租户
     * @param region 区域
     * @param env 环境
     * @return 运行时配置
     */
    public IntentRuntimeConfigDTO getConfig(String channel, String tenant, String region, String env) {
        String cacheKey = buildCacheKey(channel, tenant, region, env);
        
        // 先检查本地缓存
        IntentRuntimeConfigDTO localConfig = getFromLocalCache(cacheKey);
        if (localConfig != null) {
            log.debug("从本地缓存获取配置: key={}", cacheKey);
            return localConfig;
        }
        
        // 从Redis获取
        try {
            RBucket<IntentRuntimeConfigDTO> bucket = redissonClient.getBucket(cacheKey);
            IntentRuntimeConfigDTO redisConfig = bucket.get();
            if (redisConfig != null) {
                log.debug("从Redis缓存获取配置: key={}", cacheKey);
                // 更新本地缓存
                putToLocalCache(cacheKey, redisConfig);
                return redisConfig;
            }
        } catch (Exception e) {
            log.warn("从Redis获取配置失败: key={}, error={}", cacheKey, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 缓存运行时配置
     * 
     * @param config 运行时配置
     */
    public void cacheConfig(IntentRuntimeConfigDTO config) {
        if (config == null) {
            return;
        }
        
        String cacheKey = buildCacheKey(config.getChannel(), config.getTenant(), 
                                      config.getRegion(), config.getEnv());
        
        try {
            // 存储到Redis
            RBucket<IntentRuntimeConfigDTO> bucket = redissonClient.getBucket(cacheKey);
            bucket.set(config, CACHE_TTL);
            
            // 存储ETag
            if (config.getEtag() != null) {
                String etagKey = buildETagKey(config.getChannel(), config.getTenant(), 
                                            config.getRegion(), config.getEnv());
                RBucket<String> etagBucket = redissonClient.getBucket(etagKey);
                etagBucket.set(config.getEtag(), CACHE_TTL);
            }
            
            // 更新本地缓存
            putToLocalCache(cacheKey, config);
            
            log.info("配置缓存成功: key={}, etag={}", cacheKey, config.getEtag());
            
        } catch (Exception e) {
            log.error("配置缓存失败: key={}", cacheKey, e);
        }
    }
    
    /**
     * 检查ETag是否匹配
     * 
     * @param channel 渠道
     * @param tenant 租户
     * @param region 区域
     * @param env 环境
     * @param clientEtag 客户端ETag
     * @return 是否匹配
     */
    public boolean checkETag(String channel, String tenant, String region, String env, String clientEtag) {
        if (clientEtag == null) {
            return false;
        }
        
        String etagKey = buildETagKey(channel, tenant, region, env);
        
        try {
            RBucket<String> etagBucket = redissonClient.getBucket(etagKey);
            String cachedEtag = etagBucket.get();
            return clientEtag.equals(cachedEtag);
        } catch (Exception e) {
            log.warn("检查ETag失败: key={}, error={}", etagKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * 清除指定配置的缓存
     * 
     * @param channel 渠道
     * @param tenant 租户
     * @param region 区域
     * @param env 环境
     */
    public void evictConfig(String channel, String tenant, String region, String env) {
        String cacheKey = buildCacheKey(channel, tenant, region, env);
        String etagKey = buildETagKey(channel, tenant, region, env);
        
        try {
            // 清除Redis缓存
            redissonClient.getBucket(cacheKey).delete();
            redissonClient.getBucket(etagKey).delete();
            
            // 清除本地缓存
            localCache.remove(cacheKey);
            cacheTimestamps.remove(cacheKey);
            
            log.info("配置缓存清除成功: key={}", cacheKey);
            
        } catch (Exception e) {
            log.error("配置缓存清除失败: key={}", cacheKey, e);
        }
    }
    
    /**
     * 清除所有配置缓存
     */
    public void evictAllConfigs() {
        try {
            // 清除Redis中所有相关缓存
            Iterable<String> configKeys = redissonClient.getKeys().getKeysByPattern(CACHE_PREFIX + "*");
            Iterable<String> etagKeys = redissonClient.getKeys().getKeysByPattern(ETAG_PREFIX + "*");
            
            long configCount = redissonClient.getKeys().deleteByPattern(CACHE_PREFIX + "*");
            long etagCount = redissonClient.getKeys().deleteByPattern(ETAG_PREFIX + "*");
            
            // 清除本地缓存
            localCache.clear();
            cacheTimestamps.clear();
            
            log.info("所有配置缓存清除成功: configCount={}, etagCount={}", 
                    configCount, etagCount);
            
        } catch (Exception e) {
            log.error("清除所有配置缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        try {
            long configCount = redissonClient.getKeys().countExists(CACHE_PREFIX + "*");
            long etagCount = redissonClient.getKeys().countExists(ETAG_PREFIX + "*");
            
            return CacheStats.builder()
                    .redisConfigCount((int) configCount)
                    .redisEtagCount((int) etagCount)
                    .localCacheCount(localCache.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return CacheStats.builder().build();
        }
    }
    
    /**
     * 构建缓存键
     */
    private String buildCacheKey(String channel, String tenant, String region, String env) {
        StringBuilder key = new StringBuilder(CACHE_PREFIX);
        key.append(channel != null ? channel : "default").append(":");
        key.append(tenant != null ? tenant : "default").append(":");
        key.append(region != null ? region : "default").append(":");
        key.append(env != null ? env : "default");
        return key.toString();
    }
    
    /**
     * 构建ETag键
     */
    private String buildETagKey(String channel, String tenant, String region, String env) {
        StringBuilder key = new StringBuilder(ETAG_PREFIX);
        key.append(channel != null ? channel : "default").append(":");
        key.append(tenant != null ? tenant : "default").append(":");
        key.append(region != null ? region : "default").append(":");
        key.append(env != null ? env : "default");
        return key.toString();
    }
    
    /**
     * 从本地缓存获取配置
     */
    private IntentRuntimeConfigDTO getFromLocalCache(String key) {
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp == null || (System.currentTimeMillis() - timestamp) > LOCAL_CACHE_TTL) {
            // 缓存过期
            localCache.remove(key);
            cacheTimestamps.remove(key);
            return null;
        }
        
        return localCache.get(key);
    }
    
    /**
     * 存储到本地缓存
     */
    private void putToLocalCache(String key, IntentRuntimeConfigDTO config) {
        localCache.put(key, config);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    /**
     * 缓存统计信息
     */
    @lombok.Builder
    @lombok.Data
    public static class CacheStats {
        private int redisConfigCount;
        private int redisEtagCount;
        private int localCacheCount;
    }
}