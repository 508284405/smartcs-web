package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket回复消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReplyMessage extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型
     */
    private Integer msgType;
    
    /**
     * 发送者用户ID
     */
    private String fromUserId;
    
    /**
     * 回复的消息ID
     */
    private String replyToMsgId;
    
    /**
     * 被回复的消息内容（用于显示引用）
     */
    private String quotedContent;
    
    /**
     * 被回复的消息发送者
     */
    private String quotedFromUser;
    
    /**
     * 发送时间戳
     */
    private Long sendTime;
    
    public ReplyMessage() {
        super.setType("REPLY_MESSAGE");
    }
}