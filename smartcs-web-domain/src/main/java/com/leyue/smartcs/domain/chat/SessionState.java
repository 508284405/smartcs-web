package com.leyue.smartcs.domain.chat;

/**
 * 会话状态枚举
 */
public enum SessionState {
    /**
     * 排队中
     */
    WAITING(0),
    
    /**
     * 进行中
     */
    ACTIVE(1),
    
    /**
     * 已结束
     */
    CLOSED(2);
    
    private final int code;
    
    SessionState(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static SessionState fromCode(int code) {
        for (SessionState state : SessionState.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown session state code: " + code);
    }
}
