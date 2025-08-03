package com.leyue.smartcs.rag.persistence;

import com.leyue.smartcs.rag.persistence.model.MessageEntity;
import com.leyue.smartcs.rag.persistence.model.SessionEntity;
import com.leyue.smartcs.rag.persistence.model.MessageQuery;

import java.util.List;

/**
 * 持久化管理器接口
 * 负责消息和会话的持久化存储，支持查询和统计
 */
public interface PersistenceManager {

    /**
     * 持久化消息
     * 
     * @param message 消息实体
     */
    void persistMessage(MessageEntity message);

    /**
     * 批量持久化消息
     * 
     * @param messages 消息列表
     */
    void persistMessages(List<MessageEntity> messages);

    /**
     * 持久化会话
     * 
     * @param session 会话实体
     */
    void persistSession(SessionEntity session);

    /**
     * 查询消息
     * 
     * @param query 查询条件
     * @return 消息列表
     */
    List<MessageEntity> queryMessages(MessageQuery query);

    /**
     * 获取会话信息
     * 
     * @param sessionId 会话ID
     * @return 会话实体
     */
    SessionEntity getSession(String sessionId);

    /**
     * 更新会话状态
     * 
     * @param sessionId 会话ID
     * @param status 新状态
     */
    void updateSessionStatus(String sessionId, String status);

    /**
     * 删除会话及其相关消息
     * 
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 获取会话消息数量
     * 
     * @param sessionId 会话ID
     * @return 消息数量
     */
    long getMessageCount(String sessionId);

    /**
     * 获取用户的会话列表
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<SessionEntity> getUserSessions(String userId, int limit);

    /**
     * 清理过期的会话和消息
     * 
     * @param beforeTimestamp 时间戳（毫秒）
     * @return 清理的记录数
     */
    int cleanupExpiredData(long beforeTimestamp);

    /**
     * 检查消息是否存在
     * 
     * @param messageId 消息ID
     * @return 是否存在
     */
    boolean messageExists(String messageId);

    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean sessionExists(String sessionId);
}