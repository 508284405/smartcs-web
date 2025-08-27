package com.leyue.smartcs.domain.dictionary.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 字典条目状态枚举
 * 
 * @author Claude
 */
@Getter
@RequiredArgsConstructor
public enum EntryStatus {
    
    /**
     * 草稿状态 - 尚未生效的条目
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 活跃状态 - 正常生效的条目
     */
    ACTIVE("ACTIVE", "活跃"),
    
    /**
     * 失效状态 - 已停用的条目
     */
    INACTIVE("INACTIVE", "失效");
    
    /**
     * 状态代码
     */
    private final String code;
    
    /**
     * 状态名称
     */
    private final String name;
    
    /**
     * 根据代码获取状态枚举
     * 
     * @param code 状态代码
     * @return 状态枚举
     */
    public static EntryStatus fromCode(String code) {
        for (EntryStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的条目状态代码: " + code);
    }
}