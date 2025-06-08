package com.leyue.smartcs.domain.chat.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.enums.SessionState;
import com.leyue.smartcs.dto.chat.SessionPageQuery;

import java.util.List;
import java.util.Optional;

/**
 * 会话网关接口
 */
public interface SessionGateway {
    /**
     * 创建会话
     * 
     * @param session 会话对象
     * @return 创建后的会话ID
     */
    Long createSession(Session session);
    
    /**
     * 更新会话
     * 
     * @param session 会话对象
     * @return 是否更新成功
     */
    boolean updateSession(Session session);
    
    /**
     * 根据ID查询会话
     * 
     * @param sessionId 会话ID
     * @return 会话对象
     */
    Optional<Session> findBySessionId(Long sessionId);
    
    /**
     * 根据客户ID查询活跃会话
     * 
     * @param customerId 客户ID
     * @return 会话对象
     */
    Optional<Session> findActiveSessionByCustomerId(Long customerId);
    
    /**
     * 查询客户最新一条处理中的会话（排队或进行中）
     * 
     * @param customerId 客户ID
     * @return 会话对象，如果没有则返回null
     */
    Session findCustomerActiveSession(Long customerId);
    
    /**
     * 根据客户ID查询会话列表
     * 
     * @param customerId 客户ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<Session> findSessionsByCustomerId(Long customerId, int limit);
    
    /**
     * 根据客服ID查询活跃会话列表
     * 
     * @param agentId 客服ID
     * @return 会话列表
     */
    List<Session> findActiveSessionsByAgentId(Long agentId);
    
    /**
     * 检查会话是否存在
     * @param sessionId 会话ID（字符串形式）
     * @return 是否存在
     */
    boolean checkSessionExists(Long sessionId);
    
    /**
     * 获取会话状态
     * @param sessionId 会话ID（字符串形式）
     * @return 会话状态：WAITING-等待中，ACTIVE-进行中，CLOSED-已关闭
     */
    String getSessionStatus(Long sessionId);
    
    /**
     * 根据字符串ID获取会话信息
     * @param sessionId 会话ID（字符串形式）
     * @return 会话对象
     */
    Session getSession(Long sessionId);
    
    /**
     * 更新会话状态
     * @param sessionId 会话ID（字符串形式）
     * @param status 会话状态
     */
    void updateSessionStatus(Long sessionId, String status);
    
    /**
     * 分配客服
     * @param sessionId 会话ID（字符串形式）
     * @param agentId 客服ID（字符串形式）
     */
    void assignAgent(Long sessionId, String agentId);
    
    /**
     * 关闭会话
     * @param sessionId 会话ID（字符串形式）
     * @param reason 关闭原因
     */
    void closeSession(Long sessionId, String reason);
    
    /**
     * 分页查询会话列表
     * 
     * @param query 查询条件
     * @return 分页会话列表
     */
    PageResponse<Session> pageSessions(SessionPageQuery query);

    /**
     * 获取等待中的会话
     * @param customerId 客户ID
     * @return 会话对象
     */
    Optional<Session> getWaitingSession(Long customerId);

    /**
     * 更新会话状态
     * @param sessionId 会话ID
     * @param sessionState 会话状态
     */
    void updateSessionStatus(Long sessionId, SessionState sessionState);

    void updateSessionAgent(Long sessionId, Long targetBotId);
}
