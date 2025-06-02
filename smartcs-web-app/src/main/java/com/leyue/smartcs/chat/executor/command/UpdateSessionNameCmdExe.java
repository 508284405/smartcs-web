package com.leyue.smartcs.chat.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.domainservice.SessionDomainService;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.dto.chat.UpdateSessionNameCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 更新会话名称命令执行器
 */
@Component
@RequiredArgsConstructor
public class UpdateSessionNameCmdExe {

    private final SessionDomainService sessionDomainService;

    /**
     * 执行更新会话名称命令
     *
     * @param cmd 更新会话名称命令
     * @return 会话DTO
     */
    public SessionDTO execute(UpdateSessionNameCmd cmd) {
        Session session = sessionDomainService.updateSessionName(cmd.getSessionId(), cmd.getSessionName());
        return convertToDTO(session);
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
        sessionDTO.setSessionName(session.getSessionName());
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