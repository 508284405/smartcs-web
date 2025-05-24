package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

import java.util.Map;

/**
 * 缓存值DTO
 */
@Data
public class CacheValueDTO {
    /**
     * 缓存键名
     */
    private String cacheKey;
    
    /**
     * 缓存值（键值对形式）
     */
    private Map<String, Object> value;
    
    /**
     * 缓存过期时间（秒）
     */
    private Long ttl;
} 