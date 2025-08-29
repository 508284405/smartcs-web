package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket编辑消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EditMessage extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 新的消息内容
     */
    private String newContent;
    
    /**
     * 编辑时间戳
     */
    private Long editedAt;
    
    public EditMessage() {
        super.setType("EDIT_MESSAGE");
    }
}