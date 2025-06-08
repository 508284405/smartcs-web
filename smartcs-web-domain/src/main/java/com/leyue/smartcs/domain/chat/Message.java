package com.leyue.smartcs.domain.chat;

import java.util.Date;

import com.leyue.smartcs.domain.chat.enums.MessageType;

import lombok.Data;

/**
 * 消息领域模型
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    
    /**
     * 消息类型
     */
    private MessageType msgType;
    
    /**
     * 消息内容
     */
    private String content;

    /**
     * 聊天类型
     */
    private String chatType;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 时间戳
     */
    private Date timestamp;

    
    /**
     * 检查是否为文本消息
     */
    public boolean isTextMessage() {
        return MessageType.TEXT.equals(this.msgType);
    }
    
    /**
     * 检查是否为图片消息
     */
    public boolean isImageMessage() {
        return MessageType.IMAGE.equals(this.msgType);
    }
}
