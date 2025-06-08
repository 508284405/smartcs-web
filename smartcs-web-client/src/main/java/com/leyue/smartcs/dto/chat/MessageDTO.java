package com.leyue.smartcs.dto.chat;

import java.util.Date;

import lombok.Data;

/**
 * 消息DTO
 */
@Data
public class MessageDTO {
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 消息类型 0=text 1=image 2=order_card 3=system
     */
    private Integer msgType;
    
    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 聊天类型
     */
    private String chatType;

    /**
     * 时间戳
     */
    private Date timestamp;
}
