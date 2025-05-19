package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 删除索引命令
 */
@Data
public class DeleteIndexCmd {
    /**
     * 索引名称
     */
    private String indexName;
} 