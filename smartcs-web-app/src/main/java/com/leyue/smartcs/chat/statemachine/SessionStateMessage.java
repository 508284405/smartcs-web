package com.leyue.smartcs.chat.statemachine;

import lombok.Data;

/**
 * 会话状态变更消息
 * 用于通过WebSocket向客户端发送会话状态变更通知
 */
@Data
public class SessionStateMessage {
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 会话状态
     */
    private String status;
    
    /**
     * 状态变更时间
     */
    private Long statusChangeTime;
    
    /**
     * 客服ID
     */
    private String agentId;
    
    /**
     * 客服名称
     */
    private String agentName;
    
    /**
     * 关闭原因
     */
    private String closeReason;
} 