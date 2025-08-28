package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.OfflineMessage;

import java.util.List;

/**
 * 离线消息网关接口
 * 
 * @author Claude
 */
public interface OfflineMessageGateway {

    /**
     * 保存离线消息
     * 
     * @param offlineMessage 离线消息
     * @return 保存的离线消息ID
     */
    Long save(OfflineMessage offlineMessage);

    /**
     * 批量保存离线消息
     * 
     * @param offlineMessages 离线消息列表
     */
    void batchSave(List<OfflineMessage> offlineMessages);

    /**
     * 根据接收者ID和会话ID获取离线消息
     * 
     * @param receiverId 接收者ID
     * @param conversationId 会话ID
     * @param limit 限制条数
     * @return 离线消息列表
     */
    List<OfflineMessage> findByReceiverAndConversation(Long receiverId, String conversationId, int limit);

    /**
     * 根据接收者ID获取所有未读的离线消息摘要
     * 
     * @param receiverId 接收者ID
     * @return 离线消息摘要列表（按会话分组）
     */
    List<OfflineMessage> findUnreadSummaryByReceiver(Long receiverId);

    /**
     * 根据消息ID删除离线消息
     * 
     * @param receiverId 接收者ID
     * @param msgIds 消息ID列表
     * @return 删除的记录数
     */
    int deleteByMsgIds(Long receiverId, List<String> msgIds);

    /**
     * 根据接收者ID和会话ID清除所有离线消息
     * 
     * @param receiverId 接收者ID
     * @param conversationId 会话ID
     * @return 清除的记录数
     */
    int clearByReceiverAndConversation(Long receiverId, String conversationId);

    /**
     * 清理过期的离线消息
     * 
     * @param expireTimestamp 过期时间戳
     * @return 清理的记录数
     */
    int cleanExpiredMessages(long expireTimestamp);
}