package com.leyue.smartcs.domain.app.gateway;

import com.leyue.smartcs.domain.app.entity.AppTestSession;

import java.util.List;

/**
 * AI应用测试会话网关接口
 */
public interface AppTestSessionGateway {
    
    /**
     * 保存会话
     * @param session 会话信息
     * @return 保存的会话
     */
    AppTestSession save(AppTestSession session);
    
    /**
     * 根据会话ID查询会话
     * @param sessionId 会话ID
     * @return 会话信息
     */
    AppTestSession findBySessionId(String sessionId);
    
    /**
     * 根据应用ID查询活跃会话列表
     * @param appId 应用ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<AppTestSession> findActiveSessionsByAppId(Long appId, Integer limit);
    
    /**
     * 根据用户ID查询会话列表
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<AppTestSession> findSessionsByUserId(Long userId, Integer limit);
    
    /**
     * 更新会话统计信息
     * @param sessionId 会话ID
     * @param messageCount 消息数量
     * @param lastMessageTime 最后消息时间
     * @param totalTokens 总Token数
     * @param totalCost 总费用
     * @return 是否更新成功
     */
    boolean updateSessionStats(String sessionId, Integer messageCount, Long lastMessageTime, 
                              Integer totalTokens, java.math.BigDecimal totalCost);
    
    /**
     * 更新会话状态
     * @param sessionId 会话ID
     * @param sessionState 会话状态
     * @return 是否更新成功
     */
    boolean updateSessionState(String sessionId, String sessionState);
}