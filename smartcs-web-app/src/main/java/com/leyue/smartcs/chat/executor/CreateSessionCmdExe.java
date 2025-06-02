package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.chat.convertor.SessionConvertor;
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
    private final SessionConvertor sessionConvertor;
    
    /**
     * 执行创建会话命令
     *
     * @param cmd 创建会话命令
     * @return 会话DTO
     */
    public SessionDTO execute(CreateSessionCmd cmd) {
        // 等待中会话最多只能有一条
        Session waitingSession = sessionDomainService.getWaitingSession(cmd.getCustomerId());
        if (waitingSession != null) {
            throw new BizException("客户等待中会话最多只能有一条");
        }

        // 创建会话
        Session session = sessionDomainService.createSession(cmd.getCustomerId());
        
        // 转换为DTO
        return sessionConvertor.toDTO(session);
    }
}
