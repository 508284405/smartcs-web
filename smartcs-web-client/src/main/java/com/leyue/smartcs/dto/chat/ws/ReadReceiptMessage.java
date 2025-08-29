package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket已读回执消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReadReceiptMessage extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 读取者用户ID
     */
    private String userId;
    
    /**
     * 读取时间戳
     */
    private Long readAt;
    
    public ReadReceiptMessage() {
        super.setType("READ_RECEIPT");
    }
}