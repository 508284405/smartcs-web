package com.leyue.smartcs.domain.model.gateway;

import com.leyue.smartcs.domain.model.ModelContext;

import java.util.List;
import java.util.Optional;

/**
 * 模型上下文网关接口
 */
public interface ModelContextGateway {
    
    /**
     * 保存上下文
     * 
     * @param context 上下文对象
     * @return 保存后的上下文
     */
    ModelContext save(ModelContext context);
    
    /**
     * 根据会话ID查找上下文
     * 
     * @param sessionId 会话ID
     * @return 上下文对象
     */
    ModelContext findBySessionId(String sessionId);
    
    /**
     * 根据会话ID查找上下文（可选）
     * 
     * @param sessionId 会话ID
     * @return 上下文对象（可选）
     */
    Optional<ModelContext> findOptionalBySessionId(String sessionId);
    
    /**
     * 根据模型ID查找上下文列表
     * 
     * @param modelId 模型ID
     * @return 上下文列表
     */
    List<ModelContext> findByModelId(Long modelId);
    
    /**
     * 添加消息到上下文
     * 
     * @param sessionId 会话ID
     * @param role 消息角色
     * @param content 消息内容
     * @return 是否添加成功
     */
    boolean addMessage(String sessionId, String role, String content);
    
    /**
     * 添加用户消息
     * 
     * @param sessionId 会话ID
     * @param content 消息内容
     * @return 是否添加成功
     */
    default boolean addUserMessage(String sessionId, String content) {
        return addMessage(sessionId, "user", content);
    }
    
    /**
     * 添加助手消息
     * 
     * @param sessionId 会话ID
     * @param content 消息内容
     * @return 是否添加成功
     */
    default boolean addAssistantMessage(String sessionId, String content) {
        return addMessage(sessionId, "assistant", content);
    }
    
    /**
     * 添加系统消息
     * 
     * @param sessionId 会话ID
     * @param content 消息内容
     * @return 是否添加成功
     */
    default boolean addSystemMessage(String sessionId, String content) {
        return addMessage(sessionId, "system", content);
    }
    
    /**
     * 根据会话ID清除上下文
     * 
     * @param sessionId 会话ID
     * @return 是否清除成功
     */
    boolean clearBySessionId(String sessionId);
    
    /**
     * 根据模型ID清除上下文
     * 
     * @param modelId 模型ID
     * @return 清除数量
     */
    int clearByModelId(Long modelId);
    
    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean existsBySessionId(String sessionId);
    
    /**
     * 获取会话消息数量
     * 
     * @param sessionId 会话ID
     * @return 消息数量
     */
    int getMessageCount(String sessionId);
    
    /**
     * 获取上下文长度
     * 
     * @param sessionId 会话ID
     * @return 上下文长度
     */
    int getContextLength(String sessionId);
    
    /**
     * 删除上下文
     * 
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteBySessionId(String sessionId);
    
    /**
     * 清理过期上下文
     * 
     * @param expiredTime 过期时间（毫秒时间戳）
     * @return 清理数量
     */
    int cleanExpiredContexts(long expiredTime);
    
    /**
     * 获取活跃会话数量
     * 
     * @param modelId 模型ID（可选）
     * @return 活跃会话数量
     */
    long getActiveSessionCount(Long modelId);
}