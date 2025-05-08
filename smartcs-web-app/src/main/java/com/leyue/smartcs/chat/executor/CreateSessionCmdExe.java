package com.leyue.smartcs.chat.executor;

import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.domainservice.SessionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 创建会话命令执行器
 */
@Component
@RequiredArgsConstructor
public class CreateSessionCmdExe {
    
    private final SessionDomainService sessionDomainService;
    
    /**
     * 执行创建会话命令
     *
     * @param cmd 创建会话命令
     * @return 会话DTO
     */
    public SessionDTO execute(CreateSessionCmd cmd) {
        // 创建会话
        Session session = sessionDomainService.createSession(cmd.getCustomerId());
        
        // 转换为DTO
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setSessionId(session.getSessionId());
        sessionDTO.setCustomerId(session.getCustomerId());
        sessionDTO.setAgentId(session.getAgentId());
        sessionDTO.setSessionState(session.getSessionState().getCode());
        sessionDTO.setLastMsgTime(session.getLastMsgTime());
        sessionDTO.setCreatedAt(session.getCreatedAt());
        
        return sessionDTO;
    }
}
