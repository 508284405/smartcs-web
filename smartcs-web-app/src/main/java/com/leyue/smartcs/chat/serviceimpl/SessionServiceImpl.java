package com.leyue.smartcs.chat.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.chat.executor.CreateSessionCmdExe;
import com.leyue.smartcs.chat.executor.query.PageSessionQryExe;
import com.leyue.smartcs.chat.service.SessionService;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.domainservice.SessionDomainService;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.dto.chat.SessionPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 会话服务实现
 */
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final CreateSessionCmdExe createSessionCmdExe;
    private final PageSessionQryExe pageSessionQryExe;
    private final SessionDomainService sessionDomainService;
    private final SessionGateway sessionGateway;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public SessionDTO createSession(CreateSessionCmd createSessionCmd) {
        return createSessionCmdExe.execute(createSessionCmd);
    }

    @Override
    public SessionDTO assignAgent(Long sessionId, Long agentId) {
        return assignAgent(sessionId, agentId, null);
    }

    @Override
    public SessionDTO assignAgent(Long sessionId, Long agentId, String agentName) {
        Session session = sessionDomainService.assignAgent(sessionId, agentId, agentName);
        return convertToDTO(session);
    }

    @Override
    public SessionDTO closeSession(Long sessionId) {
        return closeSession(sessionId, null);
    }

    @Override
    public SessionDTO closeSession(Long sessionId, String reason) {
        Session session = sessionDomainService.closeSession(sessionId, reason);

        // 通知用户或客服
        webSocketSessionManager.sendToUser(session.getAgentId().toString(), "messages", "会话已关闭");
        webSocketSessionManager.sendToUser(session.getCustomerId().toString(), "messages", "会话已关闭");
        return convertToDTO(session);
    }

    @Override
    public SessionDTO getSessionDetail(Long sessionId) {
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        return convertToDTO(sessionOpt.get());
    }

    @Override
    public List<SessionDTO> getCustomerSessions(Long customerId, int limit) {
        List<Session> sessions = sessionGateway.findSessionsByCustomerId(customerId, limit);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getAgentActiveSessions(Long agentId) {
        List<Session> sessions = sessionGateway.findActiveSessionsByAgentId(agentId);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDTO getCustomerActiveSession(Long customerId) {
        Session activeSession = sessionGateway.findCustomerActiveSession(customerId);
        return activeSession != null ? convertToDTO(activeSession) : null;
    }

    @Override
    public PageResponse<SessionDTO> pageSessions(SessionPageQuery query) {
        return pageSessionQryExe.execute(query);
    }

    /**
     * 将领域模型转换为DTO
     *
     * @param session 会话领域模型
     * @return 会话DTO
     */
    private SessionDTO convertToDTO(Session session) {
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setSessionId(session.getSessionId());
        sessionDTO.setCustomerId(session.getCustomerId());
        sessionDTO.setAgentId(session.getAgentId());
        sessionDTO.setAgentName(session.getAgentName());
        sessionDTO.setSessionState(session.getSessionState().name());
        sessionDTO.setCloseReason(session.getCloseReason());
        sessionDTO.setLastMsgTime(session.getLastMsgTime());
        sessionDTO.setCreatedAt(session.getCreatedAt());
        return sessionDTO;
    }
}
