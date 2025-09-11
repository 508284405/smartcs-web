package com.leyue.smartcs.domain.intent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 快照状态枚举
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor
public enum SnapshotStatus {
    
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    ACTIVE("ACTIVE", "激活中"),
    DEPRECATED("DEPRECATED", "已弃用"),
    ROLLBACK("ROLLBACK", "已回滚"),
    ARCHIVED("ARCHIVED", "已归档");
    
    private final String code;
    private final String desc;
    
    public static SnapshotStatus fromCode(String code) {
        for (SnapshotStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的快照状态: " + code);
    }
}