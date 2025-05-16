package com.leyue.smartcs.dto.chat;

import lombok.Data;
import java.util.List;

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
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者角色 0=用户 1=客服 2=机器人
     */
    private Integer senderRole;
    
    /**
     * 消息类型 0=text 1=image 2=order_card 3=system
     */
    private Integer msgType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * @提及的用户列表
     */
    private List<Long> atList;
}
