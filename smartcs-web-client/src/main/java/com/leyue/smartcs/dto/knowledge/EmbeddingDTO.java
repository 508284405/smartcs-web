package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 文档段落向量数据传输对象
 */
@Data
public class EmbeddingDTO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 段落序号
     */
    private Integer sectionIdx;
    
    /**
     * 文本片段
     */
    private String contentSnip;
    
    /**
     * 向量数据（Base64编码）
     */
    private String vector;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
} 