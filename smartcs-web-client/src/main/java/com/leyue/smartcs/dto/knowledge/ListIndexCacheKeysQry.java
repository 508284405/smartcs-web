package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 列出索引缓存键查询
 */
@Data
public class ListIndexCacheKeysQry {
    /**
     * 索引名称
     */
    @NotEmpty(message = "索引名称不能为空")
    private String indexName;
} 