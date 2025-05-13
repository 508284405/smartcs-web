package com.leyue.smartcs.domain.bot.gateway;

import com.leyue.smartcs.domain.bot.model.Conversation;

import java.util.Optional;

/**
 * 会话网关接口
 */
public interface SessionGateway {
    
    /**
     * 保存对话
     * @param conversation 对话实体
     * @return 是否成功
     */
    boolean saveConversation(Conversation conversation);
    
    /**
     * 获取对话
     * @param sessionId 会话ID
     * @return 对话实体
     */
    Optional<Conversation> getConversation(String sessionId);
    
    /**
     * 删除对话
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean deleteConversation(String sessionId);
    
    /**
     * 获取用户会话信息
     * @param sessionId 会话ID
     * @return 用户信息
     */
    Optional<String> getUserInfo(String sessionId);
} 