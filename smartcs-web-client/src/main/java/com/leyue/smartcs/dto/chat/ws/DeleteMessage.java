package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket删除消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeleteMessage extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 操作用户ID
     */
    private String userId;
    
    /**
     * 删除类型 0-仅自己可见删除 1-双方删除
     */
    private Integer deleteType;
    
    /**
     * 删除原因
     */
    private String reason;
    
    /**
     * 删除时间
     */
    private Long deleteTime;
    
    public DeleteMessage() {
        super.setType("DELETE");
    }
}