package com.leyue.smartcs.domain.chat.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.enums.SessionState;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 会话领域服务
 */
@Service
@RequiredArgsConstructor
public class SessionDomainService {

    private final SessionGateway sessionGateway;
    private final IdGeneratorGateway idGeneratorGateway;

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
        session.setSessionId(idGeneratorGateway.generateId());
        session.setCustomerId(customerId);
        session.setSessionState(SessionState.WAITING);
        session.setLastMsgTime(System.currentTimeMillis());

        Long sessionId = sessionGateway.createSession(session);
        session.setSessionId(sessionId);

        return session;
    }

    /**
     * 分配客服
     *
     * @param sessionId 会话ID
     * @param agentId   客服ID
     * @return 更新后的会话
     */
    public Session assignAgent(Long sessionId, Long agentId) {
        return assignAgent(sessionId, agentId, null);
    }

    /**
     * 分配客服（带客服名称）
     *
     * @param sessionId 会话ID
     * @param agentId   客服ID
     * @param agentName 客服名称
     * @return 更新后的会话
     */
    public Session assignAgent(Long sessionId, Long agentId, String agentName) {
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        Session session = sessionOpt.get();
        if (!session.isWaiting()) {
            throw new BizException("会话状态不是等待中，无法分配客服");
        }

        session.assignAgent(agentId, agentName);
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
        return closeSession(sessionId, null);
    }

    /**
     * 关闭会话（带关闭原因）
     *
     * @param sessionId 会话ID
     * @param reason    关闭原因
     * @return 关闭后的会话
     */
    public Session closeSession(Long sessionId, String reason) {
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        Session session = sessionOpt.get();
        if (session.isClosed()) {
            return session;
        }

        if (reason != null) {
            session.close(reason);
        } else {
            session.close();
        }

        sessionGateway.updateSession(session);

        return session;
    }

    /**
     * 更新会话最后消息时间
     *
     * @param sessionId 会话ID
     * @param time      时间
     */
    public void updateLastMessageTime(Long sessionId, Long time) {
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        Session session = sessionOpt.get();
        session.updateLastMessageTime(time);
        sessionGateway.updateSession(session);
    }

    public Session getWaitingSession(Long customerId) {
        Optional<Session> sessionOpt = sessionGateway.getWaitingSession(customerId);
        if (sessionOpt.isPresent()) {
            return sessionOpt.get();
        }
        return null;
    }
}
