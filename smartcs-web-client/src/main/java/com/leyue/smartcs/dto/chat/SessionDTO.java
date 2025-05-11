package com.leyue.smartcs.dto.chat;

import lombok.Data;

/**
 * 会话数据传输对象
 */
@Data
public class SessionDTO {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客服ID
     */
    private Long agentId;
    
    /**
     * 客服名称
     */
    private String agentName;
    
    /**
     * 会话状态
     */
    private String sessionState;
    
    /**
     * 关闭原因
     */
    private String closeReason;
    
    /**
     * 最后消息内容
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private Long lastMsgTime;
    
    /**
     * 创建时间
     */
    private Long createdAt;
}
