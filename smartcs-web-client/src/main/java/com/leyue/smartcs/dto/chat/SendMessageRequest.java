package com.leyue.smartcs.dto.chat;

import lombok.Data;

/**
 * 发送消息请求对象
 */
@Data
public class SendMessageRequest {
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
}
