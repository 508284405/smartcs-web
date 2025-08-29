package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket消息状态更新通知
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageStatusUpdate extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 发送状态 0-发送中 1-已送达 2-发送失败 3-已读
     */
    private Integer sendStatus;
    
    /**
     * 状态描述文本
     */
    private String statusText;
    
    /**
     * 状态图标
     */
    private String statusIcon;
    
    /**
     * 失败原因（发送失败时）
     */
    private String failReason;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 是否可以重试
     */
    private Boolean canRetry;
    
    /**
     * 更新时间戳
     */
    private Long updatedAt;
    
    public MessageStatusUpdate() {
        super.setType("MESSAGE_STATUS_UPDATE");
    }
}