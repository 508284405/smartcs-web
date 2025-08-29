package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.ConversationSettings;
import java.util.List;

/**
 * 会话设置网关接口
 */
public interface ConversationSettingsGateway {
    
    /**
     * 保存会话设置
     */
    ConversationSettings save(ConversationSettings settings);
    
    /**
     * 根据ID查找设置
     */
    ConversationSettings findById(Long id);
    
    /**
     * 根据用户和会话查找设置
     */
    ConversationSettings findByUserAndSession(String userId, Long sessionId);
    
    /**
     * 获取用户的所有会话设置
     */
    List<ConversationSettings> findByUserId(String userId);
    
    /**
     * 获取用户置顶的会话
     */
    List<ConversationSettings> findPinnedConversations(String userId);
    
    /**
     * 获取用户免打扰的会话
     */
    List<ConversationSettings> findMutedConversations(String userId);
    
    /**
     * 获取用户归档的会话
     */
    List<ConversationSettings> findArchivedConversations(String userId);
    
    /**
     * 获取用户未归档的会话设置
     */
    List<ConversationSettings> findActiveConversations(String userId);
    
    /**
     * 批量更新过期的免打扰设置
     */
    int updateExpiredMuteSettings();
    
    /**
     * 统计用户置顶会话数量
     */
    long countPinnedConversations(String userId);
    
    /**
     * 统计用户归档会话数量
     */
    long countArchivedConversations(String userId);
    
    /**
     * 删除会话设置
     */
    boolean deleteById(Long id);
    
    /**
     * 删除用户的会话设置
     */
    boolean deleteByUserAndSession(String userId, Long sessionId);
    
    /**
     * 批量删除用户的会话设置
     */
    int deleteByUserId(String userId);
}