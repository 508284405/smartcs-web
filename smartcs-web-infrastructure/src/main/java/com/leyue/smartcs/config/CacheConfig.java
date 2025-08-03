package com.leyue.smartcs.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache配置
 * 使用Redisson作为缓存后端
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * 配置缓存管理器
     * 使用Redisson作为缓存后端
     */
    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        log.info("配置Redisson缓存管理器");
        
        // 使用默认配置，缓存TTL由Redisson自动管理
        return new RedissonSpringCacheManager(redissonClient);
    }
} 