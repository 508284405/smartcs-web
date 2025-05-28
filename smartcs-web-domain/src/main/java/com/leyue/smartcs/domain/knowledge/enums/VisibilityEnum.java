package com.leyue.smartcs.domain.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 可见性枚举
 */
@Getter
@AllArgsConstructor
public enum VisibilityEnum {
    
    PUBLIC("public", "公开"),
    PRIVATE("private", "私有");
    
    private final String code;
    private final String description;
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举实例
     */
    public static VisibilityEnum fromCode(String code) {
        for (VisibilityEnum visibility : values()) {
            if (visibility.getCode().equals(code)) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("未知的可见性类型: " + code);
    }
    
    /**
     * 检查代码是否有效
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (VisibilityEnum visibility : values()) {
            if (visibility.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
} 