package com.leyue.smartcs.domain.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话领域模型
 */
@Data
public class Session {
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
     * 会话状态
     */
    private SessionState sessionState;
    
    /**
     * 最后消息时间
     */
    private LocalDateTime lastMsgTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 检查会话是否活跃
     */
    public boolean isActive() {
        return SessionState.ACTIVE.equals(this.sessionState);
    }
    
    /**
     * 检查会话是否已关闭
     */
    public boolean isClosed() {
        return SessionState.CLOSED.equals(this.sessionState);
    }
    
    /**
     * 检查会话是否在等待中
     */
    public boolean isWaiting() {
        return SessionState.WAITING.equals(this.sessionState);
    }
    
    /**
     * 更新最后消息时间
     */
    public void updateLastMessageTime(LocalDateTime time) {
        this.lastMsgTime = time;
    }
    
    /**
     * 分配客服
     */
    public void assignAgent(Long agentId) {
        this.agentId = agentId;
        this.sessionState = SessionState.ACTIVE;
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        this.sessionState = SessionState.CLOSED;
    }
}
