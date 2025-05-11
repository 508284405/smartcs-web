package com.leyue.smartcs.chat.assembler;

import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.SessionState;
import com.leyue.smartcs.dto.chat.SessionDTO;
import org.springframework.stereotype.Component;

/**
 * 会话对象转换器
 */
@Component
public class SessionAssembler {

    /**
     * 将领域模型转换为DTO
     *
     * @param session 会话领域模型
     * @return 会话DTO
     */
    public SessionDTO toDTO(Session session) {
        if (session == null) {
            return null;
        }
        
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setSessionId(session.getSessionId());
        sessionDTO.setCustomerId(session.getCustomerId());
        sessionDTO.setAgentId(session.getAgentId());
        sessionDTO.setAgentName(session.getAgentName());
        
        // 转换会话状态
        if (session.getSessionState() != null) {
            sessionDTO.setSessionState(session.getSessionState().name());
        }
        
        sessionDTO.setCloseReason(session.getCloseReason());
        sessionDTO.setLastMsgTime(session.getLastMsgTime());
        sessionDTO.setLastMessage(session.getLastMessage());
        sessionDTO.setCreatedAt(session.getCreatedAt());
        return sessionDTO;
    }
    
    /**
     * 将DTO转换为领域模型
     *
     * @param sessionDTO 会话DTO
     * @return 会话领域模型
     */
    public Session toDomain(SessionDTO sessionDTO) {
        if (sessionDTO == null) {
            return null;
        }
        
        Session session = new Session();
        session.setSessionId(sessionDTO.getSessionId());
        session.setCustomerId(sessionDTO.getCustomerId());
        session.setAgentId(sessionDTO.getAgentId());
        session.setAgentName(sessionDTO.getAgentName());
        
        // 转换会话状态
        if (sessionDTO.getSessionState() != null) {
            try {
                session.setSessionState(SessionState.valueOf(sessionDTO.getSessionState()));
            } catch (IllegalArgumentException e) {
                // 状态码转换失败时，默认为等待状态
                session.setSessionState(SessionState.WAITING);
            }
        }
        
        session.setCloseReason(sessionDTO.getCloseReason());
        session.setLastMsgTime(sessionDTO.getLastMsgTime());
        session.setLastMessage(sessionDTO.getLastMessage());
        session.setCreatedAt(sessionDTO.getCreatedAt());
        return session;
    }
} 