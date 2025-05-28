package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 内容DTO
 */
@Data
public class ContentDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 所属知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容类型 document/audio/video
     */
    private String contentType;
    
    /**
     * 原始文件地址
     */
    private String fileUrl;
    
    /**
     * 提取后的原始文本
     */
    private String textExtracted;
    
    /**
     * 状态 uploaded/parsed/vectorized
     */
    private String status;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
} 