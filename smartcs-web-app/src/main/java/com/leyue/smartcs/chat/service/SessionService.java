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
     * 分配客服（带客服名称）
     *
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @param agentName 客服名称
     * @return 会话DTO
     */
    SessionDTO assignAgent(Long sessionId, Long agentId, String agentName);
    
    /**
     * 关闭会话
     *
     * @param sessionId 会话ID
     * @return 会话DTO
     */
    SessionDTO closeSession(Long sessionId);
    
    /**
     * 关闭会话（带关闭原因）
     *
     * @param sessionId 会话ID
     * @param reason 关闭原因
     * @return 会话DTO
     */
    SessionDTO closeSession(Long sessionId, String reason);
    
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
    
    /**
     * 获取客户最新一条处理中的会话（排队或进行中）
     *
     * @param customerId 客户ID
     * @return 会话DTO，如果没有则返回null
     */
    SessionDTO getCustomerActiveSession(Long customerId);
}
