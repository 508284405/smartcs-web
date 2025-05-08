package com.leyue.smartcs.domain.chat;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息领域模型
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private Long msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者角色
     */
    private SenderRole senderRole;
    
    /**
     * 消息类型
     */
    private MessageType msgType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * @提及的用户列表
     */
    private List<Long> atList;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
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
    
    /**
     * 检查消息是否来自客服
     */
    public boolean isFromAgent() {
        return SenderRole.AGENT.equals(this.senderRole);
    }
    
    /**
     * 检查消息是否来自用户
     */
    public boolean isFromUser() {
        return SenderRole.USER.equals(this.senderRole);
    }
    
    /**
     * 检查消息是否来自机器人
     */
    public boolean isFromBot() {
        return SenderRole.BOT.equals(this.senderRole);
    }
}
