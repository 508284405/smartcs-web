package com.leyue.smartcs.domain.intent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 版本状态枚举
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor
public enum VersionStatus {
    
    DRAFT("DRAFT", "草稿"),
    REVIEW("REVIEW", "审核中"),
    ACTIVE("ACTIVE", "激活"),
    DEPRECATED("DEPRECATED", "已废弃");
    
    private final String code;
    private final String desc;
    
    public static VersionStatus fromCode(String code) {
        for (VersionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的版本状态: " + code);
    }
}