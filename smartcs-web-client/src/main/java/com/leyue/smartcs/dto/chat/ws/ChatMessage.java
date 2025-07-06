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
    private String messageType;

    /**
     * 聊天类型，User
     */
    private String chatType;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    public ChatMessage() {
        super.setType("CHAT");
    }
}
