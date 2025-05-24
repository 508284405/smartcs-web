package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 触发文档向量生成命令
 */
@Data
public class TriggerDocEmbeddingCmd {
    
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 解析器名称
     */
    private String strategyName;
} 