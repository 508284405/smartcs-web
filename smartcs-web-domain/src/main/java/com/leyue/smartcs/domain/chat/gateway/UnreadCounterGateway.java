package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.UnreadCounter;

import java.util.List;
import java.util.Map;

/**
 * 未读计数网关接口
 * 
 * @author Claude
 */
public interface UnreadCounterGateway {

    /**
     * 保存或更新未读计数
     * 
     * @param unreadCounter 未读计数
     * @return 保存的未读计数ID
     */
    Long saveOrUpdate(UnreadCounter unreadCounter);

    /**
     * 增加未读计数
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param increment 增量
     * @return 更新后的计数
     */
    int incrementUnreadCount(Long userId, String conversationId, int increment);

    /**
     * 减少未读计数
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param decrement 减量
     * @return 更新后的计数
     */
    int decrementUnreadCount(Long userId, String conversationId, int decrement);

    /**
     * 清零未读计数
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 操作是否成功
     */
    boolean resetUnreadCount(Long userId, String conversationId);

    /**
     * 获取用户的未读计数
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 未读计数，不存在则返回0
     */
    int getUnreadCount(Long userId, String conversationId);

    /**
     * 获取用户的所有会话未读计数
     * 
     * @param userId 用户ID
     * @return 会话ID -> 未读数 的映射
     */
    Map<String, Integer> getAllUnreadCounts(Long userId);

    /**
     * 批量获取用户未读计数
     * 
     * @param userId 用户ID
     * @param conversationIds 会话ID列表
     * @return 未读计数列表
     */
    List<UnreadCounter> findByUserAndConversations(Long userId, List<String> conversationIds);

    /**
     * 删除用户的未读计数记录
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 操作是否成功
     */
    boolean deleteUnreadCounter(Long userId, String conversationId);
}