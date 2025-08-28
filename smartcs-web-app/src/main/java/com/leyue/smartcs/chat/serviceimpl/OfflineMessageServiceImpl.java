package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.api.OfflineMessageService;
import com.leyue.smartcs.api.UnreadCounterService;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.OfflineMessage;
import com.leyue.smartcs.domain.chat.gateway.OfflineMessageGateway;
import com.leyue.smartcs.dto.chat.offline.OfflineMessageAckCmd;
import com.leyue.smartcs.dto.chat.offline.OfflineMessageSummaryDto;
import com.leyue.smartcs.dto.chat.offline.OfflineMessagesDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 离线消息服务实现
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfflineMessageServiceImpl implements OfflineMessageService {

    private final OfflineMessageGateway offlineMessageGateway;
    private final UnreadCounterService unreadCounterService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public List<OfflineMessageSummaryDto> getOfflineMessageSummary(Long userId) {
        log.info("获取用户离线消息摘要: userId={}", userId);
        
        // 获取离线消息摘要
        List<OfflineMessage> offlineMessages = offlineMessageGateway.findUnreadSummaryByReceiver(userId);
        
        // 获取未读计数
        Map<String, Integer> unreadCounts = unreadCounterService.getAllUnreadCounts(userId);
        
        return offlineMessages.stream()
                .map(offlineMessage -> OfflineMessageSummaryDto.builder()
                        .conversationId(offlineMessage.getConversationId())
                        .unreadCount(unreadCounts.getOrDefault(offlineMessage.getConversationId(), 0))
                        .lastMessageBrief(offlineMessage.getMsgBrief())
                        .lastMessageTime(offlineMessage.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public OfflineMessagesDto getOfflineMessages(Long userId, String conversationId, int limit) {
        log.info("获取离线消息详情: userId={}, conversationId={}, limit={}", userId, conversationId, limit);
        
        List<OfflineMessage> offlineMessages = offlineMessageGateway.findByReceiverAndConversation(userId, conversationId, limit);
        
        List<OfflineMessagesDto.OfflineMessageDto> messageDtos = offlineMessages.stream()
                .map(offlineMessage -> OfflineMessagesDto.OfflineMessageDto.builder()
                        .msgId(offlineMessage.getMsgId())
                        .msgBrief(offlineMessage.getMsgBrief())
                        .createdAt(offlineMessage.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return OfflineMessagesDto.builder()
                .conversationId(conversationId)
                .messages(messageDtos)
                .hasMore(messageDtos.size() >= limit)
                .build();
    }

    @Override
    public boolean ackOfflineMessages(OfflineMessageAckCmd ackCmd) {
        log.info("确认离线消息: {}", ackCmd);
        
        try {
            // 删除离线消息
            int deletedCount = offlineMessageGateway.deleteByMsgIds(ackCmd.getUserId(), ackCmd.getMsgIds());
            
            // 减少未读计数
            if (deletedCount > 0) {
                unreadCounterService.decrementUnreadCount(ackCmd.getUserId(), ackCmd.getConversationId(), deletedCount);
            }
            
            return true;
        } catch (Exception e) {
            log.error("确认离线消息失败: {}", ackCmd, e);
            return false;
        }
    }

    @Override
    public boolean clearOfflineMessages(Long userId, String conversationId) {
        log.info("清除离线消息: userId={}, conversationId={}", userId, conversationId);
        
        try {
            // 清除离线消息
            int clearedCount = offlineMessageGateway.clearByReceiverAndConversation(userId, conversationId);
            
            // 重置未读计数
            if (clearedCount > 0) {
                unreadCounterService.resetUnreadCount(userId, conversationId);
            }
            
            return true;
        } catch (Exception e) {
            log.error("清除离线消息失败: userId={}, conversationId={}", userId, conversationId, e);
            return false;
        }
    }

    @Override
    public void saveOfflineMessage(Long receiverId, String conversationId, String msgId, String msgBrief) {
        log.debug("保存离线消息: receiverId={}, conversationId={}, msgId={}", receiverId, conversationId, msgId);
        
        // 创建离线消息
        OfflineMessage offlineMessage = OfflineMessage.create(receiverId, conversationId, msgId, msgBrief);
        
        // 保存离线消息
        offlineMessageGateway.save(offlineMessage);
        
        // 增加未读计数
        unreadCounterService.incrementUnreadCount(receiverId, conversationId, 1);
    }

    @Override
    public void processUserOnline(Long userId) {
        log.info("处理用户上线离线消息: userId={}", userId);
        
        // 获取用户离线消息摘要
        List<OfflineMessageSummaryDto> summaries = getOfflineMessageSummary(userId);
        
        if (!summaries.isEmpty()) {
            // 通过WebSocket推送离线消息摘要
            try {
                sessionManager.sendToUser(String.valueOf(userId), "offline-summary", summaries);
                log.info("向用户推送离线消息摘要: userId={}, count={}", userId, summaries.size());
            } catch (Exception e) {
                log.error("推送离线消息摘要失败: userId={}", userId, e);
            }
        }
    }
}