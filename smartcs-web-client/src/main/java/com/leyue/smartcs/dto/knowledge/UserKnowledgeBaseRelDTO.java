package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 用户知识库权限关系DTO
 */
@Data
public class UserKnowledgeBaseRelDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 角色 reader/writer/admin
     */
    private String role;
    
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