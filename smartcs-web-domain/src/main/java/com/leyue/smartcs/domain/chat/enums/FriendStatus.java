package com.leyue.smartcs.domain.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友状态枚举
 */
@Getter
@AllArgsConstructor
public enum FriendStatus {
    
    PENDING(0, "待审核"),
    ACCEPTED(1, "已同意"),
    REJECTED(2, "已拒绝"),
    BLOCKED(3, "已拉黑");
    
    private final Integer code;
    private final String description;
    
    /**
     * 根据状态码获取枚举
     */
    public static FriendStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (FriendStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 根据状态码获取描述
     */
    public static String getDescription(Integer code) {
        FriendStatus status = fromCode(code);
        return status != null ? status.getDescription() : "未知状态";
    }
    
    /**
     * 检查状态是否有效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
    
    /**
     * 获取所有可用状态
     */
    public static FriendStatus[] getAvailableStatuses() {
        return values();
    }
}