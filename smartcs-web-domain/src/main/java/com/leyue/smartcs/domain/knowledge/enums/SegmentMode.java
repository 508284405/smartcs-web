package com.leyue.smartcs.domain.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分段模式枚举
 */
@Getter
@AllArgsConstructor
public enum SegmentMode {
    
    GENERAL("general", "通用"),
    PARENT_CHILD("parent_child", "父子分段");
    
    private final String code;
    private final String description;
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举实例
     */
    public static SegmentMode fromCode(String code) {
        for (SegmentMode mode : values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("未知的分段模式: " + code);
    }
    
    /**
     * 检查代码是否有效
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (SegmentMode mode : values()) {
            if (mode.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
} 