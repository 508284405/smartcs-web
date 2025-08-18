package com.leyue.smartcs.domain.intent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 意图状态枚举
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor
public enum IntentStatus {
    
    DRAFT("DRAFT", "草稿"),
    ACTIVE("ACTIVE", "激活"),
    DEPRECATED("DEPRECATED", "已废弃");
    
    private final String code;
    private final String desc;
    
    public static IntentStatus fromCode(String code) {
        for (IntentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的意图状态: " + code);
    }
}