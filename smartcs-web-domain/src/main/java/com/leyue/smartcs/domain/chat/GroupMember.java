package com.leyue.smartcs.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 群成员领域实体
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 群ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色：OWNER/ADMIN/MEMBER
     */
    private String role;
    
    /**
     * 加群时间
     */
    private Long joinedAt;
    
    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
    
    /**
     * 群成员角色枚举
     */
    public enum Role {
        OWNER("OWNER", "群主"),
        ADMIN("ADMIN", "管理员"),
        MEMBER("MEMBER", "普通成员");
        
        private final String code;
        private final String desc;
        
        Role(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static Role fromCode(String code) {
            for (Role role : values()) {
                if (role.code.equals(code)) {
                    return role;
                }
            }
            return MEMBER; // 默认为普通成员
        }
    }
    
    /**
     * 创建群成员
     */
    public static GroupMember create(Long groupId, Long userId, Role role) {
        return GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(role.getCode())
                .joinedAt(System.currentTimeMillis())
                .isDeleted(0)
                .build();
    }
    
    /**
     * 创建群主
     */
    public static GroupMember createOwner(Long groupId, Long userId) {
        return create(groupId, userId, Role.OWNER);
    }
    
    /**
     * 创建普通成员
     */
    public static GroupMember createMember(Long groupId, Long userId) {
        return create(groupId, userId, Role.MEMBER);
    }
    
    /**
     * 提升为管理员
     */
    public void promoteToAdmin() {
        this.role = Role.ADMIN.getCode();
    }
    
    /**
     * 降级为普通成员
     */
    public void demoteToMember() {
        this.role = Role.MEMBER.getCode();
    }
    
    /**
     * 标记为已删除（退群）
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
    }
    
    /**
     * 检查是否已退群
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted == 1;
    }
    
    /**
     * 检查是否为群主
     */
    public boolean isOwner() {
        return Role.OWNER.getCode().equals(this.role);
    }
    
    /**
     * 检查是否为管理员或群主
     */
    public boolean isAdminOrOwner() {
        return Role.ADMIN.getCode().equals(this.role) || Role.OWNER.getCode().equals(this.role);
    }
    
    /**
     * 获取角色枚举
     */
    public Role getRoleEnum() {
        return Role.fromCode(this.role);
    }
}