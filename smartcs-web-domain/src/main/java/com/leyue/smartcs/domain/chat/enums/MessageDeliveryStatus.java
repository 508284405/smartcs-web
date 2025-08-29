package com.leyue.smartcs.domain.chat.enums;

/**
 * 消息投递状态枚举
 * 
 * @author Claude
 * @since 2024-08-29
 */
public enum MessageDeliveryStatus {
    
    /**
     * 待发送
     */
    PENDING(0, "待发送"),
    
    /**
     * 发送中
     */
    SENDING(1, "发送中"),
    
    /**
     * 已投递到服务器
     */
    DELIVERED_TO_SERVER(2, "已投递到服务器"),
    
    /**
     * 已推送给接收者
     */
    PUSHED_TO_RECEIVER(3, "已推送给接收者"),
    
    /**
     * 接收者已确认
     */
    ACKNOWLEDGED(4, "接收者已确认"),
    
    /**
     * 投递失败
     */
    FAILED(5, "投递失败"),
    
    /**
     * 需要重试
     */
    RETRY_REQUIRED(6, "需要重试"),
    
    /**
     * 达到最大重试次数
     */
    MAX_RETRY_REACHED(7, "达到最大重试次数");
    
    private final Integer code;
    private final String description;
    
    MessageDeliveryStatus(Integer code, String description) {
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
     * 是否为最终状态（不需要进一步处理）
     */
    public boolean isFinalStatus() {
        return this == ACKNOWLEDGED || this == MAX_RETRY_REACHED;
    }
    
    /**
     * 是否需要重试
     */
    public boolean needsRetry() {
        return this == FAILED || this == RETRY_REQUIRED;
    }
    
    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return this != MAX_RETRY_REACHED && this != ACKNOWLEDGED;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static MessageDeliveryStatus fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        
        for (MessageDeliveryStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return PENDING;
    }
}