package com.leyue.smartcs.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 群组领域实体
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 群ID
     */
    private Long groupId;
    
    /**
     * 群名称
     */
    private String groupName;
    
    /**
     * 群主用户ID
     */
    private Long ownerId;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
    
    /**
     * 创建群组
     */
    public static Group create(Long groupId, String groupName, Long ownerId) {
        long now = System.currentTimeMillis();
        return Group.builder()
                .groupId(groupId)
                .groupName(groupName)
                .ownerId(ownerId)
                .createdAt(now)
                .updatedAt(now)
                .isDeleted(0)
                .build();
    }
    
    /**
     * 更新群名称
     */
    public void updateName(String newName) {
        this.groupName = newName;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted == 1;
    }
    
    /**
     * 检查用户是否为群主
     */
    public boolean isOwner(Long userId) {
        return this.ownerId != null && this.ownerId.equals(userId);
    }
}