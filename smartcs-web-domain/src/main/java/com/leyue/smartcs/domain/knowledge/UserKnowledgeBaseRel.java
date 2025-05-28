package com.leyue.smartcs.domain.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户知识库权限关系领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserKnowledgeBaseRel {
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
    
    /**
     * 检查是否有读权限
     * @return 是否有读权限
     */
    public boolean canRead() {
        return "reader".equals(this.role) || "writer".equals(this.role) || "admin".equals(this.role);
    }
    
    /**
     * 检查是否有写权限
     * @return 是否有写权限
     */
    public boolean canWrite() {
        return "writer".equals(this.role) || "admin".equals(this.role);
    }
    
    /**
     * 检查是否有管理员权限
     * @return 是否有管理员权限
     */
    public boolean isAdmin() {
        return "admin".equals(this.role);
    }
    
    /**
     * 检查角色是否有效
     * @return 是否有效
     */
    public boolean isValidRole() {
        return "reader".equals(this.role) || "writer".equals(this.role) || "admin".equals(this.role);
    }
} 