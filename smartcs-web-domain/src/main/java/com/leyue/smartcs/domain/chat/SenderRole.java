package com.leyue.smartcs.domain.chat;

/**
 * 发送者角色枚举
 */
public enum SenderRole {
    /**
     * 用户
     */
    USER(0),
    
    /**
     * 客服
     */
    AGENT(1),
    
    /**
     * 机器人
     */
    BOT(2);
    
    private final int code;
    
    SenderRole(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static SenderRole fromCode(int code) {
        for (SenderRole role : SenderRole.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown sender role code: " + code);
    }
}
