package com.leyue.smartcs.domain.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档向量生成事件
 * 用于在领域层发布文档需要进行向量生成的事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocEmbeddingGenerateEvent {
    
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 触发时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 触发来源，用于记录事件的发起方
     */
    private String source;
    
    /**
     * 解析策略名称，用于指定文档解析方式
     */
    private String strategyName;
} 