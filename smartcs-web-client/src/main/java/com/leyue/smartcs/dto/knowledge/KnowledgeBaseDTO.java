package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 知识库DTO
 */
@Data
public class KnowledgeBaseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 知识库唯一编码
     */
    private String code;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 创建者ID
     */
    private Long ownerId;
    
    /**
     * 可见性 public/private
     */
    private String visibility;
    
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