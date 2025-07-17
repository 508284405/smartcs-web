package com.leyue.smartcs.domain.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分段规则配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentationRule {
    
    /**
     * 最大token数
     */
    private int maxTokens;
    
    /**
     * 重叠大小
     */
    private int chunkOverlap;
    
    /**
     * 最小块大小
     */
    private int minChunkSize;
    
    /**
     * 最大块大小
     */
    private int maxChunkSize;
    
    /**
     * 分隔符
     */
    private String separator;
    
    /**
     * 是否保留分隔符
     */
    private boolean keepSeparator;
    
    /**
     * 父块大小（用于父子分段）
     */
    private int parentChunkSize;
    
    /**
     * 父块重叠大小
     */
    private int parentOverlapSize;
    
    /**
     * 子块大小
     */
    private int childChunkSize;
    
    /**
     * 子块重叠大小
     */
    private int childOverlapSize;
    
    /**
     * 上下文段落数
     */
    private int contextParagraphs;
} 