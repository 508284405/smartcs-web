package com.leyue.smartcs.dto.chat;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 回复消息命令
 */
@Data
public class ReplyMessageCmd {
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private String sessionId;
    
    /**
     * 发送者用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String fromUserId;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型
     */
    private Integer msgType;
    
    /**
     * 回复的消息ID
     */
    @NotBlank(message = "回复消息ID不能为空")
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
}