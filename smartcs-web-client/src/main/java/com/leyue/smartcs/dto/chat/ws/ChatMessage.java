package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 发送者ID
     */
    private String fromUserId;
    
    /**
     * 发送者类型，0-客户，1-客服
     */
    private String fromUserType;
    
    /**
     * 发送者名称
     */
    private String fromUserName;
    
    /**
     * 接收者ID
     */
    private String toUserId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息内容类型，TEXT-文本，IMAGE-图片，FILE-文件，AUDIO-音频，VIDEO-视频
     */
    private String contentType;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    public ChatMessage() {
        setType("CHAT");
    }
}
