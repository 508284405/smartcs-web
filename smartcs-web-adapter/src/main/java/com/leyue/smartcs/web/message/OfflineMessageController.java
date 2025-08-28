package com.leyue.smartcs.web.message;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.OfflineMessageService;
import com.leyue.smartcs.api.UnreadCounterService;
import com.leyue.smartcs.dto.chat.offline.OfflineMessageAckCmd;
import com.leyue.smartcs.dto.chat.offline.OfflineMessageSummaryDto;
import com.leyue.smartcs.dto.chat.offline.OfflineMessagesDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 离线消息管理控制器
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/im/offline")
public class OfflineMessageController {
    
    private final OfflineMessageService offlineMessageService;
    private final UnreadCounterService unreadCounterService;

    /**
     * 获取用户离线消息摘要
     *
     * @param userId 用户ID
     * @return 离线消息摘要列表
     */
    @GetMapping("/summary")
    public MultiResponse<OfflineMessageSummaryDto> getOfflineMessageSummary(@RequestParam Long userId) {
        log.info("获取离线消息摘要: userId={}", userId);
        
        try {
            List<OfflineMessageSummaryDto> summaries = offlineMessageService.getOfflineMessageSummary(userId);
            return MultiResponse.of(summaries);
        } catch (Exception e) {
            log.error("获取离线消息摘要失败: userId={}", userId, e);
            return MultiResponse.buildFailure("OFFLINE_MSG_001", "获取离线消息摘要失败");
        }
    }

    /**
     * 获取指定会话的离线消息详情
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param limit 限制条数，默认20
     * @return 离线消息详情
     */
    @GetMapping("/{conversationId}")
    public SingleResponse<OfflineMessagesDto> getOfflineMessages(
            @PathVariable String conversationId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("获取离线消息详情: userId={}, conversationId={}, limit={}", userId, conversationId, limit);
        
        try {
            OfflineMessagesDto offlineMessages = offlineMessageService.getOfflineMessages(userId, conversationId, limit);
            return SingleResponse.of(offlineMessages);
        } catch (Exception e) {
            log.error("获取离线消息详情失败: userId={}, conversationId={}", userId, conversationId, e);
            return SingleResponse.buildFailure("OFFLINE_MSG_002", "获取离线消息详情失败");
        }
    }

    /**
     * 确认离线消息已读
     *
     * @param ackCmd 确认命令
     * @return 操作结果
     */
    @PostMapping("/ack")
    public Response ackOfflineMessages(@RequestBody OfflineMessageAckCmd ackCmd) {
        log.info("确认离线消息已读: {}", ackCmd);
        
        try {
            boolean success = offlineMessageService.ackOfflineMessages(ackCmd);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("OFFLINE_MSG_003", "确认离线消息失败");
            }
        } catch (Exception e) {
            log.error("确认离线消息失败: {}", ackCmd, e);
            return Response.buildFailure("OFFLINE_MSG_003", "确认离线消息失败");
        }
    }

    /**
     * 清除指定会话的所有离线消息
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{conversationId}")
    public Response clearOfflineMessages(
            @PathVariable String conversationId,
            @RequestParam Long userId) {
        
        log.info("清除离线消息: userId={}, conversationId={}", userId, conversationId);
        
        try {
            boolean success = offlineMessageService.clearOfflineMessages(userId, conversationId);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("OFFLINE_MSG_004", "清除离线消息失败");
            }
        } catch (Exception e) {
            log.error("清除离线消息失败: userId={}, conversationId={}", userId, conversationId, e);
            return Response.buildFailure("OFFLINE_MSG_004", "清除离线消息失败");
        }
    }

    /**
     * 获取用户的所有会话未读计数
     *
     * @param userId 用户ID
     * @return 未读计数映射
     */
    @GetMapping("/unread-counts")
    public SingleResponse<Map<String, Integer>> getUnreadCounts(@RequestParam Long userId) {
        log.info("获取未读计数: userId={}", userId);
        
        try {
            Map<String, Integer> unreadCounts = unreadCounterService.getAllUnreadCounts(userId);
            return SingleResponse.of(unreadCounts);
        } catch (Exception e) {
            log.error("获取未读计数失败: userId={}", userId, e);
            return SingleResponse.buildFailure("OFFLINE_MSG_005", "获取未读计数失败");
        }
    }

    /**
     * 获取指定会话的未读计数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 未读计数
     */
    @GetMapping("/unread-count")
    public SingleResponse<Integer> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam String conversationId) {
        
        log.debug("获取会话未读计数: userId={}, conversationId={}", userId, conversationId);
        
        try {
            int unreadCount = unreadCounterService.getUnreadCount(userId, conversationId);
            return SingleResponse.of(unreadCount);
        } catch (Exception e) {
            log.error("获取会话未读计数失败: userId={}, conversationId={}", userId, conversationId, e);
            return SingleResponse.buildFailure("OFFLINE_MSG_006", "获取会话未读计数失败");
        }
    }

    /**
     * 重置指定会话的未读计数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @PostMapping("/reset-unread")
    public Response resetUnreadCount(
            @RequestParam Long userId,
            @RequestParam String conversationId) {
        
        log.info("重置未读计数: userId={}, conversationId={}", userId, conversationId);
        
        try {
            boolean success = unreadCounterService.resetUnreadCount(userId, conversationId);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("OFFLINE_MSG_007", "重置未读计数失败");
            }
        } catch (Exception e) {
            log.error("重置未读计数失败: userId={}, conversationId={}", userId, conversationId, e);
            return Response.buildFailure("OFFLINE_MSG_007", "重置未读计数失败");
        }
    }
}