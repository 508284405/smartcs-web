package com.leyue.smartcs.domain.chat.enums;

/**
 * 消息发送状态枚举
 */
public enum MessageSendStatus {
    
    /**
     * 发送中
     */
    SENDING(0, "发送中"),
    
    /**
     * 已送达
     */
    DELIVERED(1, "已送达"),
    
    /**
     * 发送失败
     */
    SEND_FAILED(2, "发送失败"),
    
    /**
     * 已读
     */
    READ(3, "已读");

    private final Integer code;
    private final String description;

    MessageSendStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static MessageSendStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageSendStatus status : MessageSendStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown MessageSendStatus code: " + code);
    }
}