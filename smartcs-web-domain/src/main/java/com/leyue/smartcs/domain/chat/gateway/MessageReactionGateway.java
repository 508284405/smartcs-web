package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.MessageReaction;

import java.util.List;
import java.util.Optional;

/**
 * 消息表情反应网关接口
 */
public interface MessageReactionGateway {
    
    /**
     * 添加表情反应
     */
    MessageReaction addReaction(MessageReaction reaction);
    
    /**
     * 移除表情反应
     */
    boolean removeReaction(String msgId, String userId, String emoji);
    
    /**
     * 切换表情反应（存在则删除，不存在则添加）
     */
    boolean toggleReaction(String msgId, String sessionId, String userId, String emoji, String name);
    
    /**
     * 查询消息的所有表情反应
     */
    List<MessageReaction> findByMsgId(String msgId);
    
    /**
     * 查询用户对特定消息的表情反应
     */
    List<MessageReaction> findByMsgIdAndUserId(String msgId, String userId);
    
    /**
     * 查询特定表情反应
     */
    Optional<MessageReaction> findByMsgIdAndUserIdAndEmoji(String msgId, String userId, String emoji);
    
    /**
     * 统计消息的表情反应数量
     */
    int countReactionsByMsgId(String msgId);
    
    /**
     * 统计特定表情的反应数量
     */
    int countReactionsByMsgIdAndEmoji(String msgId, String emoji);
    
    /**
     * 批量删除消息的所有表情反应（当消息被删除时）
     */
    boolean deleteAllReactionsByMsgId(String msgId);
    
    /**
     * 更新消息表情反应统计
     */
    boolean updateMessageReactionSummary(String msgId);
}