package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 正在输入提示消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TypingMessage extends WebSocketMessage {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户昵称
     */
    private String userName;
    
    /**
     * 是否正在输入 true-开始输入 false-停止输入
     */
    private Boolean isTyping;
    
    /**
     * 输入开始时间戳
     */
    private Long startTime;
    
    public TypingMessage() {
        super.setType("TYPING");
    }
}