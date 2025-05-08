package com.leyue.smartcs.chat.service;

import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;

import java.util.List;

/**
 * 会话服务接口
 */
public interface SessionService {
    
    /**
     * 创建会话
     *
     * @param createSessionCmd 创建会话命令
     * @return 会话DTO
     */
    SessionDTO createSession(CreateSessionCmd createSessionCmd);
    
    /**
     * 分配客服
     *
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @return 会话DTO
     */
    SessionDTO assignAgent(Long sessionId, Long agentId);
    
    /**
     * 关闭会话
     *
     * @param sessionId 会话ID
     * @return 会话DTO
     */
    SessionDTO closeSession(Long sessionId);
    
    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话DTO
     */
    SessionDTO getSessionDetail(Long sessionId);
    
    /**
     * 获取客户的会话列表
     *
     * @param customerId 客户ID
     * @param limit 限制数量
     * @return 会话DTO列表
     */
    List<SessionDTO> getCustomerSessions(Long customerId, int limit);
    
    /**
     * 获取客服的活跃会话列表
     *
     * @param agentId 客服ID
     * @return 会话DTO列表
     */
    List<SessionDTO> getAgentActiveSessions(Long agentId);
}
