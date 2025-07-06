package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息确认
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AckMessage extends WebSocketMessage {
    
    /**
     * 原始消息ID
     */
    private String originalMsgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 确认状态：SUCCESS-成功，FAIL-失败
     */
    private String status;
    
    /**
     * 错误代码，失败时有值
     */
    private String errorCode;
    
    /**
     * 错误信息，失败时有值
     */
    private String errorMessage;
    
    public AckMessage() {
        super.setType("ACK");
    }
}
