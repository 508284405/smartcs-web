package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket消息撤回消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecallMessage extends WebSocketMessage {
    
    /**
     * 被撤回的消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 撤回操作者ID
     */
    private String userId;
    
    /**
     * 撤回原因
     */
    private String reason;
    
    /**
     * 撤回时间
     */
    private Long recallTime;
    
    public RecallMessage() {
        super.setType("RECALL");
    }
}