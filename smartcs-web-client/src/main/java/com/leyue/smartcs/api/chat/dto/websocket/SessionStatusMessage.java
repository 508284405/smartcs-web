package com.leyue.smartcs.api.chat.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话状态消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SessionStatusMessage extends WebSocketMessage {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 会话状态：WAITING-等待中，ACTIVE-进行中，CLOSED-已关闭
     */
    private String status;
    
    /**
     * 客服ID，仅ACTIVE状态有值
     */
    private String agentId;
    
    /**
     * 客服名称，仅ACTIVE状态有值
     */
    private String agentName;
    
    /**
     * 关闭原因，仅CLOSED状态有值
     */
    private String closeReason;
    
    /**
     * 状态变更时间
     */
    private Long statusChangeTime;
    
    public SessionStatusMessage() {
        setType("SESSION_STATUS");
    }
}
