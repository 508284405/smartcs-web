package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 清空索引缓存命令
 */
@Data
public class ClearIndexCacheCmd {
    /**
     * 索引名称
     */
    @NotEmpty(message = "索引名称不能为空")
    private String indexName;
} 