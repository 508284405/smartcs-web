package com.leyue.smartcs.domain.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色权限枚举
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {
    
    READER("reader", "读者", 1),
    WRITER("writer", "写者", 2),
    ADMIN("admin", "管理员", 3);
    
    private final String code;
    private final String description;
    private final int level; // 权限级别，数字越大权限越高
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举实例
     */
    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色类型: " + code);
    }
    
    /**
     * 检查代码是否有效
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否有读权限
     * @return 是否有读权限
     */
    public boolean canRead() {
        return this.level >= READER.level;
    }
    
    /**
     * 检查是否有写权限
     * @return 是否有写权限
     */
    public boolean canWrite() {
        return this.level >= WRITER.level;
    }
    
    /**
     * 检查是否有管理员权限
     * @return 是否有管理员权限
     */
    public boolean canAdmin() {
        return this.level >= ADMIN.level;
    }
    
    /**
     * 检查当前角色是否包含目标角色的权限
     * @param targetRole 目标角色
     * @return 是否包含权限
     */
    public boolean hasPermission(RoleEnum targetRole) {
        return this.level >= targetRole.level;
    }
} 