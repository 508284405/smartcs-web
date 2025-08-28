package com.leyue.smartcs.api;

import java.util.Map;

/**
 * 未读计数服务接口
 * 
 * @author Claude
 */
public interface UnreadCounterService {

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
     * 重置未读计数为0
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
     * @return 未读计数
     */
    int getUnreadCount(Long userId, String conversationId);

    /**
     * 获取用户的所有会话未读计数
     * 
     * @param userId 用户ID
     * @return 会话ID -> 未读数 的映射
     */
    Map<String, Integer> getAllUnreadCounts(Long userId);
}