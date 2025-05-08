package com.leyue.smartcs.domain.chat.domainservice;

import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.SessionState;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 会话领域服务
 */
@Service
@RequiredArgsConstructor
public class SessionDomainService {
    
    private final SessionGateway sessionGateway;
    
    /**
     * 创建新会话
     *
     * @param customerId 客户ID
     * @return 新建的会话
     */
    public Session createSession(Long customerId) {
        // 检查是否已有活跃会话
        Optional<Session> existingSession = sessionGateway.findActiveSessionByCustomerId(customerId);
        if (existingSession.isPresent()) {
            return existingSession.get();
        }
        
        // 创建新会话
        Session session = new Session();
        session.setCustomerId(customerId);
        session.setSessionState(SessionState.WAITING);
        session.setLastMsgTime(LocalDateTime.now());
        
        Long sessionId = sessionGateway.createSession(session);
        session.setSessionId(sessionId);
        
        return session;
    }
    
    /**
     * 分配客服
     *
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @return 更新后的会话
     */
    public Session assignAgent(Long sessionId, Long agentId) {
        Optional<Session> sessionOpt = sessionGateway.findById(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        
        Session session = sessionOpt.get();
        if (!session.isWaiting()) {
            throw new IllegalStateException("会话状态不是等待中，无法分配客服");
        }
        
        session.assignAgent(agentId);
        sessionGateway.updateSession(session);
        
        return session;
    }
    
    /**
     * 关闭会话
     *
     * @param sessionId 会话ID
     * @return 关闭后的会话
     */
    public Session closeSession(Long sessionId) {
        Optional<Session> sessionOpt = sessionGateway.findById(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        
        Session session = sessionOpt.get();
        if (session.isClosed()) {
            return session;
        }
        
        session.close();
        sessionGateway.updateSession(session);
        
        return session;
    }
    
    /**
     * 更新会话最后消息时间
     *
     * @param sessionId 会话ID
     * @param time 时间
     */
    public void updateLastMessageTime(Long sessionId, LocalDateTime time) {
        Optional<Session> sessionOpt = sessionGateway.findById(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        
        Session session = sessionOpt.get();
        session.updateLastMessageTime(time);
        sessionGateway.updateSession(session);
    }
}
