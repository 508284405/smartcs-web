package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 获取缓存值查询
 */
@Data
public class GetCacheValueQry {
    /**
     * 缓存键名
     */
    @NotEmpty(message = "缓存键名不能为空")
    private String cacheKey;
} 