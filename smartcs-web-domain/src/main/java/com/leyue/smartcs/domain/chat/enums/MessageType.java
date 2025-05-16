package com.leyue.smartcs.domain.chat.enums;

/**
 * 消息类型枚举
 */
public enum MessageType {
    /**
     * 文本消息
     */
    TEXT(0),
    
    /**
     * 图片消息
     */
    IMAGE(1),
    
    /**
     * 订单卡片
     */
    ORDER_CARD(2),
    
    /**
     * 系统消息
     */
    SYSTEM(3);
    
    private final int code;
    
    MessageType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static MessageType fromCode(int code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}
