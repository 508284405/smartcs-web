package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.dto.chat.SessionPageQuery;
import com.leyue.smartcs.dto.chat.UpdateSessionNameCmd;

import java.util.List;

/**
 * 会话服务接口
 */
public interface SessionService {
    
    /**
     * 创建会话
     *
     * @param cmd 创建会话命令
     * @return 会话DTO
     */
    SessionDTO createSession(CreateSessionCmd cmd);
    
    /**
     * 分配客服
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
     * 获取客户最新一条处理中的会话
     *
     * @param customerId 客户ID
     * @return 会话DTO
     */
    SessionDTO getCustomerActiveSession(Long customerId);
    
    /**
     * 分页查询会话列表
     *
     * @param query 查询参数
     * @return 分页会话数据
     */
    PageResponse<SessionDTO> pageSessions(SessionPageQuery query);
    
    /**
     * 更新会话名称
     *
     * @param cmd 更新会话名称命令
     * @return 会话DTO
     */
    SessionDTO updateSessionName(UpdateSessionNameCmd cmd);
} 