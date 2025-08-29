package com.leyue.smartcs.domain.chat.enums;

/**
 * 用户在线状态枚举
 */
public enum UserStatus {
    /**
     * 在线
     */
    ONLINE("online", "在线"),
    
    /**
     * 离线
     */
    OFFLINE("offline", "离线"),
    
    /**
     * 忙碌
     */
    BUSY("busy", "忙碌"),
    
    /**
     * 离开
     */
    AWAY("away", "离开"),
    
    /**
     * 隐身
     */
    INVISIBLE("invisible", "隐身");
    
    private final String code;
    private final String description;
    
    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static UserStatus fromCode(String code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return OFFLINE; // 默认离线状态
    }
}