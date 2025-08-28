package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.chat.offline.OfflineMessageSummaryDto;
import com.leyue.smartcs.dto.chat.offline.OfflineMessagesDto;
import com.leyue.smartcs.dto.chat.offline.OfflineMessageAckCmd;

import java.util.List;

/**
 * 离线消息服务接口
 * 
 * @author Claude
 */
public interface OfflineMessageService {

    /**
     * 获取用户离线消息摘要
     * 
     * @param userId 用户ID
     * @return 离线消息摘要列表（按会话分组）
     */
    List<OfflineMessageSummaryDto> getOfflineMessageSummary(Long userId);

    /**
     * 获取指定会话的离线消息详情
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param limit 限制条数
     * @return 离线消息详情
     */
    OfflineMessagesDto getOfflineMessages(Long userId, String conversationId, int limit);

    /**
     * 确认离线消息已读
     * 
     * @param ackCmd 确认命令
     * @return 操作是否成功
     */
    boolean ackOfflineMessages(OfflineMessageAckCmd ackCmd);

    /**
     * 清除指定会话的所有离线消息
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 操作是否成功
     */
    boolean clearOfflineMessages(Long userId, String conversationId);

    /**
     * 保存离线消息
     * 
     * @param receiverId 接收者ID
     * @param conversationId 会话ID
     * @param msgId 消息ID
     * @param msgBrief 消息摘要
     */
    void saveOfflineMessage(Long receiverId, String conversationId, String msgId, String msgBrief);

    /**
     * 用户上线时处理离线消息推送
     * 
     * @param userId 用户ID
     */
    void processUserOnline(Long userId);
}