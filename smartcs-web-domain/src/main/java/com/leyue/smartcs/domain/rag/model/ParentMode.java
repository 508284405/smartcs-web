package com.leyue.smartcs.domain.rag.model;

/**
 * 父子分段模式枚举
 */
public enum ParentMode {
    /**
     * 段落模式 - 父块为段落，子块为更小的片段
     */
    PARAGRAPH,
    
    /**
     * 全文档模式 - 父块为整个文档，子块为段落
     */
    FULL_DOC
} 