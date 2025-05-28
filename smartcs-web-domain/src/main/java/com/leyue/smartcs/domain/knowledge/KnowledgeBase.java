package com.leyue.smartcs.domain.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBase {
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
    
    /**
     * 检查是否为公开知识库
     * @return 是否公开
     */
    public boolean isPublic() {
        return "public".equals(this.visibility);
    }
    
    /**
     * 检查用户是否为知识库所有者
     * @param userId 用户ID
     * @return 是否为所有者
     */
    public boolean isOwner(Long userId) {
        return this.ownerId != null && this.ownerId.equals(userId);
    }
    
    /**
     * 检查知识库名称是否有效
     * @return 是否有效
     */
    public boolean isValidName() {
        return this.name != null && !this.name.trim().isEmpty() && this.name.length() <= 128;
    }
    
    /**
     * 检查知识库编码是否有效
     * @return 是否有效
     */
    public boolean isValidCode() {
        return this.code != null && !this.code.trim().isEmpty() && this.code.length() <= 64;
    }
} 