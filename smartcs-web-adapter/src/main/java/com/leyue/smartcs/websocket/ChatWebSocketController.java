package com.leyue.smartcs.websocket;

import com.leyue.smartcs.dto.chat.ws.AckMessage;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import com.leyue.smartcs.dto.chat.ws.WebSocketMessage;
import com.leyue.smartcs.dto.chat.ws.RecallMessage;
import com.leyue.smartcs.dto.chat.ws.TypingMessage;
import com.leyue.smartcs.dto.chat.ws.UserStatusMessage;
import com.leyue.smartcs.dto.chat.ws.DeleteMessage;
import com.leyue.smartcs.dto.chat.DeleteMessageCmd;
import com.leyue.smartcs.dto.chat.ws.EditMessage;
import com.leyue.smartcs.dto.chat.EditMessageCmd;
import com.leyue.smartcs.dto.chat.ws.ReadReceiptMessage;
import com.leyue.smartcs.dto.chat.MarkMessageReadCmd;
import com.leyue.smartcs.dto.chat.ws.MessageStatusUpdate;
import com.leyue.smartcs.dto.chat.ws.ReactionMessage;
import com.leyue.smartcs.dto.chat.RetryMessageSendCmd;
import com.leyue.smartcs.dto.chat.AddReactionCmd;
import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.api.MessageSendService;
import com.leyue.smartcs.api.MessageValidatorService;
import com.leyue.smartcs.api.GroupChatService;
import com.leyue.smartcs.dto.chat.group.GroupMessageCmd;
import com.leyue.smartcs.dto.chat.RecallMessageCmd;
import com.leyue.smartcs.chat.executor.RecallMessageCmdExe;
import com.leyue.smartcs.chat.service.TypingStatusService;
import com.leyue.smartcs.chat.service.UserStatusService;
import com.leyue.smartcs.domain.chat.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket消息控制器
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final WebSocketSessionManager sessionManager;
    private final MessageSendService messageSendService;
    private final MessageValidatorService messageValidatorService;
    private final GroupChatService groupChatService;
    private final RecallMessageCmdExe recallMessageCmdExe;
    private final TypingStatusService typingStatusService;
    private final UserStatusService userStatusService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户或客服发送的聊天消息
     * 客户端发送消息到: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    @SendToUser("/queue/reply")
    public AckMessage sendMessage(@Payload ChatMessage chatMessage,
                                  SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        log.info("接收到WebSocket消息: userId={}, sessionId={}, message={}", userId, sessionId, chatMessage);

        // 设置消息ID（如果客户端未提供）
        if (chatMessage.getMsgId() == null) {
            chatMessage.setMsgId(UUID.randomUUID().toString());
        }

        // 设置发送者ID和时间
//        chatMessage.setFromUserId(String.valueOf(userId));
        chatMessage.setCreateTime(System.currentTimeMillis());

        // 检查用户类型，从会话属性中获取
//        if (headerAccessor.getSessionAttributes() != null) {
//            Object userType = headerAccessor.getSessionAttributes().get("userType");
//            if (userType != null) {
//                chatMessage.setFromUserType(userType.toString());
//            }
//        }

        // 验证消息
        AckMessage ackMessage = new AckMessage();
        ackMessage.setOriginalMsgId(chatMessage.getMsgId());
        ackMessage.setSessionId(chatMessage.getSessionId());

        try {
            // 验证消息
            messageValidatorService.validate(chatMessage);

            // 发送消息
            messageSendService.send(chatMessage);

            // 注册会话状态
            if (userId != null && sessionId != null && !sessionManager.isUserOnline(userId)) {
                String userType = "CUSTOMER";

                // 安全获取userType
                java.util.Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
                if (sessionAttrs != null && sessionAttrs.get("userType") != null) {
                    userType = sessionAttrs.get("userType").toString();
                }
                sessionManager.registerSession(String.valueOf(userId), sessionId, userType);
            }

            // 返回确认消息
            ackMessage.setStatus("SUCCESS");
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage(), e);
            ackMessage.setStatus("FAIL");
            ackMessage.setErrorCode("MESSAGE_SEND_FAILED");
            ackMessage.setErrorMessage(e.getMessage());
        }

        return ackMessage;
    }

    /**
     * 处理群聊消息
     * 客户端发送消息到: /app/group.sendMessage
     */
    @MessageMapping("/group.sendMessage")
    @SendToUser("/queue/reply")
    public AckMessage sendGroupMessage(@Payload ChatMessage chatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        log.info("接收到群聊WebSocket消息: userId={}, sessionId={}, message={}", userId, sessionId, chatMessage);

        // 设置消息ID（如果客户端未提供）
        if (chatMessage.getMsgId() == null) {
            chatMessage.setMsgId(java.util.UUID.randomUUID().toString());
        }

        AckMessage ackMessage = new AckMessage();
        ackMessage.setOriginalMsgId(chatMessage.getMsgId());

        try {
            // 验证消息基本字段
            if (chatMessage.getGroupId() == null) {
                throw new IllegalArgumentException("群组ID不能为空");
            }

            // 构建群消息命令
            GroupMessageCmd groupMessageCmd = GroupMessageCmd.builder()
                    .groupId(chatMessage.getGroupId())
                    .senderId(userId)
                    .content(chatMessage.getContent())
                    .messageType(chatMessage.getChatType())
                    .msgId(chatMessage.getMsgId())
                    .build();

            // 发送群消息
            groupChatService.sendGroupMessage(groupMessageCmd);

            // 注册会话状态
            if (userId != null && sessionId != null && !sessionManager.isUserOnline(userId)) {
                String userType = "CUSTOMER";
                java.util.Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
                if (sessionAttrs != null && sessionAttrs.get("userType") != null) {
                    userType = sessionAttrs.get("userType").toString();
                }
                sessionManager.registerSession(String.valueOf(userId), sessionId, userType);
            }

            ackMessage.setStatus("SUCCESS");
        } catch (Exception e) {
            log.error("发送群消息失败: {}", e.getMessage(), e);
            ackMessage.setStatus("FAIL");
            ackMessage.setErrorCode("GROUP_MESSAGE_SEND_FAILED");
            ackMessage.setErrorMessage(e.getMessage());
        }

        return ackMessage;
    }

    /**
     * 处理消息确认
     * 客户端发送消息到: /app/chat.ack
     */
    @MessageMapping("/chat.ack")
    public void handleAck(@Payload AckMessage ackMessage, Principal principal) {
        String userId = principal.getName();
        log.info("接收到消息确认: userId={}, ackMessage={}", userId, ackMessage);

        // 处理消息确认，可以记录消息已读状态等
        // 这里简化处理，实际项目可能需要更复杂的逻辑
    }

    /**
     * 处理心跳消息
     * 客户端发送消息到: /app/chat.heartbeat
     */
    @MessageMapping("/chat.heartbeat")
    @SendToUser("/queue/heartbeat")
    public WebSocketMessage handleHeartbeat(@Header("simpSessionId") String sessionId, SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        log.debug("接收到心跳消息: userId={}, sessionId={}", userId, sessionId);

        // 返回心跳响应
        AckMessage heartbeatResponse = new AckMessage();
        heartbeatResponse.setStatus("SUCCESS");
        heartbeatResponse.setOriginalMsgId("heartbeat");
        return heartbeatResponse;
    }
    
    /**
     * 处理消息撤回
     * 客户端发送消息到: /app/chat.recallMessage
     */
    @MessageMapping("/chat.recallMessage")
    @SendToUser("/queue/reply")
    public AckMessage recallMessage(@Payload RecallMessage recallMessage, 
                                   SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        log.info("接收到消息撤回请求: userId={}, sessionId={}, msgId={}", 
                userId, sessionId, recallMessage.getMsgId());

        AckMessage ackMessage = new AckMessage();
        ackMessage.setOriginalMsgId(recallMessage.getMsgId());
        ackMessage.setSessionId(recallMessage.getSessionId());

        try {
            // 构建撤回命令
            RecallMessageCmd recallCmd = new RecallMessageCmd();
            recallCmd.setMsgId(recallMessage.getMsgId());
            recallCmd.setSessionId(recallMessage.getSessionId());
            recallCmd.setUserId(String.valueOf(userId));
            recallCmd.setReason(recallMessage.getReason());

            // 执行撤回
            com.alibaba.cola.dto.Response response = recallMessageCmdExe.execute(recallCmd);
            
            if (response.isSuccess()) {
                ackMessage.setStatus("SUCCESS");
                log.info("消息撤回成功: msgId={}", recallMessage.getMsgId());
            } else {
                ackMessage.setStatus("FAIL");
                ackMessage.setErrorCode(response.getErrCode());
                ackMessage.setErrorMessage(response.getErrMessage());
            }

        } catch (Exception e) {
            log.error("消息撤回失败: {}", e.getMessage(), e);
            ackMessage.setStatus("FAIL");
            ackMessage.setErrorCode("RECALL_FAILED");
            ackMessage.setErrorMessage(e.getMessage());
        }

        return ackMessage;
    }
    
    /**
     * 处理输入状态变化
     * 客户端发送消息到: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingMessage typingMessage, 
                           SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.debug("接收到输入状态变化: userId={}, sessionId={}, typing={}", 
                userId, sessionId, typingMessage.getIsTyping());
        
        // 设置消息中的用户信息
        typingMessage.setUserId(String.valueOf(userId));
        typingMessage.setStartTime(System.currentTimeMillis());
        
        try {
            if (Boolean.TRUE.equals(typingMessage.getIsTyping())) {
                // 用户开始输入
                typingStatusService.setUserTyping(typingMessage.getSessionId(), String.valueOf(userId));
            } else {
                // 用户停止输入
                typingStatusService.removeUserTyping(typingMessage.getSessionId(), String.valueOf(userId));
            }
            
            // 广播输入状态给会话中的其他用户
            broadcastTypingStatus(typingMessage);
            
        } catch (Exception e) {
            log.error("处理输入状态失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 广播输入状态给会话中的其他用户
     */
    private void broadcastTypingStatus(TypingMessage typingMessage) {
        try {
            // 向会话频道广播输入状态
            String destination = "/topic/typing/" + typingMessage.getSessionId();
            messagingTemplate.convertAndSend(destination, typingMessage);
            
            log.debug("输入状态已广播: sessionId={}, userId={}, typing={}", 
                    typingMessage.getSessionId(), typingMessage.getUserId(), typingMessage.getIsTyping());
                    
        } catch (Exception e) {
            log.error("广播输入状态失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户状态变更
     * 客户端发送消息到: /app/chat.userStatus
     */
    @MessageMapping("/chat.userStatus")
    public void handleUserStatus(@Payload UserStatusMessage userStatusMessage, 
                                SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.debug("接收到用户状态变更: userId={}, sessionId={}, status={}", 
                userId, sessionId, userStatusMessage.getStatus());
        
        // 设置消息中的用户信息
        userStatusMessage.setUserId(String.valueOf(userId));
        userStatusMessage.setLastSeenAt(System.currentTimeMillis());
        
        try {
            // 解析状态枚举
            UserStatus status = UserStatus.fromCode(userStatusMessage.getStatus());
            
            // 更新用户状态
            userStatusService.setUserStatus(String.valueOf(userId), status, 
                    userStatusMessage.getStatusMessage());
            
            // 广播状态变更给相关用户
            broadcastUserStatus(userStatusMessage);
            
        } catch (Exception e) {
            log.error("处理用户状态变更失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 广播用户状态变更
     */
    private void broadcastUserStatus(UserStatusMessage userStatusMessage) {
        try {
            // 向全局用户状态频道广播
            String destination = "/topic/user/status";
            messagingTemplate.convertAndSend(destination, userStatusMessage);
            
            log.debug("用户状态变更已广播: userId={}, status={}", 
                    userStatusMessage.getUserId(), userStatusMessage.getStatus());
                    
        } catch (Exception e) {
            log.error("广播用户状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理删除消息请求
     * 客户端发送消息到: /app/chat.deleteMessage
     */
    @MessageMapping("/chat.deleteMessage")
    @SendToUser("/queue/reply")
    public AckMessage deleteMessage(@Payload DeleteMessage deleteMessage, 
                                  SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.info("收到删除消息请求: msgId={}, userId={}, sessionId={}", 
                deleteMessage.getMsgId(), userId, sessionId);
        
        try {
            // 构造删除命令
            DeleteMessageCmd cmd = new DeleteMessageCmd();
            cmd.setMsgId(deleteMessage.getMsgId());
            cmd.setSessionId(deleteMessage.getSessionId());
            cmd.setUserId(String.valueOf(userId));
            cmd.setDeleteType(deleteMessage.getDeleteType());
            cmd.setReason(deleteMessage.getReason());
            
            // 执行删除
            Response response = messageService.deleteMessage(cmd);
            
            if (response.isSuccess()) {
                return createSuccessAck(deleteMessage.getMsgId(), deleteMessage.getSessionId());
            } else {
                return createErrorAck(deleteMessage.getMsgId(), deleteMessage.getSessionId(), 
                                    "DELETE_FAILED", response.getErrMessage());
            }
            
        } catch (Exception e) {
            log.error("删除消息失败: {}", e.getMessage(), e);
            return createErrorAck(deleteMessage.getMsgId(), deleteMessage.getSessionId(), 
                                "DELETE_ERROR", e.getMessage());
        }
    }

    /**
     * 处理编辑消息请求
     * 客户端发送消息到: /app/chat.editMessage
     */
    @MessageMapping("/chat.editMessage")
    @SendToUser("/queue/reply")
    public AckMessage editMessage(@Payload EditMessage editMessage, 
                                SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.info("收到编辑消息请求: msgId={}, userId={}, sessionId={}", 
                editMessage.getMsgId(), userId, sessionId);
        
        try {
            // 构造编辑命令
            EditMessageCmd cmd = new EditMessageCmd();
            cmd.setMsgId(editMessage.getMsgId());
            cmd.setSessionId(editMessage.getSessionId());
            cmd.setUserId(String.valueOf(userId));
            cmd.setNewContent(editMessage.getNewContent());
            cmd.setEditedAt(System.currentTimeMillis());
            
            // 执行编辑
            Response response = messageService.editMessage(cmd);
            
            if (response.isSuccess()) {
                return createSuccessAck(editMessage.getMsgId(), editMessage.getSessionId());
            } else {
                return createErrorAck(editMessage.getMsgId(), editMessage.getSessionId(), 
                                    "EDIT_FAILED", response.getErrMessage());
            }
            
        } catch (Exception e) {
            log.error("编辑消息失败: {}", e.getMessage(), e);
            return createErrorAck(editMessage.getMsgId(), editMessage.getSessionId(), 
                                "EDIT_ERROR", e.getMessage());
        }
    }

    /**
     * 处理标记消息已读请求
     * 客户端发送消息到: /app/chat.markRead
     */
    @MessageMapping("/chat.markRead")
    @SendToUser("/queue/reply")
    public AckMessage markMessageAsRead(@Payload ReadReceiptMessage readReceiptMessage, 
                                      SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.info("收到标记消息已读请求: msgId={}, userId={}, sessionId={}", 
                readReceiptMessage.getMsgId(), userId, sessionId);
        
        try {
            // 构造标记已读命令
            MarkMessageReadCmd cmd = new MarkMessageReadCmd();
            cmd.setMsgId(readReceiptMessage.getMsgId());
            cmd.setSessionId(readReceiptMessage.getSessionId());
            cmd.setUserId(String.valueOf(userId));
            cmd.setReadAt(System.currentTimeMillis());
            
            // 执行标记已读
            Response response = messageService.markMessageAsRead(cmd);
            
            if (response.isSuccess()) {
                return createSuccessAck(readReceiptMessage.getMsgId(), readReceiptMessage.getSessionId());
            } else {
                return createErrorAck(readReceiptMessage.getMsgId(), readReceiptMessage.getSessionId(), 
                                    "MARK_READ_FAILED", response.getErrMessage());
            }
            
        } catch (Exception e) {
            log.error("标记消息已读失败: {}", e.getMessage(), e);
            return createErrorAck(readReceiptMessage.getMsgId(), readReceiptMessage.getSessionId(), 
                                "MARK_READ_ERROR", e.getMessage());
        }
    }

    /**
     * 处理重试发送消息请求
     * 客户端发送消息到: /app/chat.retryMessage
     */
    @MessageMapping("/chat.retryMessage")
    @SendToUser("/queue/reply")
    public AckMessage retryMessageSend(@Payload MessageStatusUpdate statusMessage, 
                                     SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.info("收到重试发送消息请求: msgId={}, userId={}, sessionId={}", 
                statusMessage.getMsgId(), userId, sessionId);
        
        try {
            // 构造重试发送命令
            RetryMessageSendCmd cmd = new RetryMessageSendCmd();
            cmd.setMsgId(statusMessage.getMsgId());
            cmd.setSessionId(statusMessage.getSessionId());
            cmd.setUserId(String.valueOf(userId));
            cmd.setRetryAt(System.currentTimeMillis());
            
            // 执行重试发送
            Response response = messageService.retryMessageSend(cmd);
            
            if (response.isSuccess()) {
                return createSuccessAck(statusMessage.getMsgId(), statusMessage.getSessionId());
            } else {
                return createErrorAck(statusMessage.getMsgId(), statusMessage.getSessionId(), 
                                    "RETRY_FAILED", response.getErrMessage());
            }
            
        } catch (Exception e) {
            log.error("重试发送消息失败: {}", e.getMessage(), e);
            return createErrorAck(statusMessage.getMsgId(), statusMessage.getSessionId(), 
                                "RETRY_ERROR", e.getMessage());
        }
    }

    /**
     * 处理表情反应
     * 客户端发送消息到: /app/chat.reaction
     */
    @MessageMapping("/chat.reaction")
    public AckMessage handleReaction(@Payload ReactionMessage reactionMessage, 
                                   SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        log.info("收到表情反应请求: msgId={}, userId={}, sessionId={}, emoji={}, action={}", 
                reactionMessage.getMsgId(), userId, sessionId, 
                reactionMessage.getEmoji(), reactionMessage.getAction());
        
        try {
            // 构造表情反应命令
            AddReactionCmd cmd = new AddReactionCmd();
            cmd.setMsgId(reactionMessage.getMsgId());
            cmd.setSessionId(reactionMessage.getSessionId());
            cmd.setEmoji(reactionMessage.getEmoji());
            cmd.setName(reactionMessage.getName());
            cmd.setAction(reactionMessage.getAction() != null ? reactionMessage.getAction() : "toggle");
            
            // 执行表情反应
            Response response = messageService.addReaction(cmd);
            
            if (response.isSuccess()) {
                // 广播表情反应更新
                broadcastReactionUpdate(reactionMessage, String.valueOf(userId));
                return createSuccessAck(reactionMessage.getMsgId(), reactionMessage.getSessionId());
            } else {
                return createErrorAck(reactionMessage.getMsgId(), reactionMessage.getSessionId(), 
                                    "REACTION_FAILED", response.getErrMessage());
            }
            
        } catch (Exception e) {
            log.error("处理表情反应失败: {}", e.getMessage(), e);
            return createErrorAck(reactionMessage.getMsgId(), reactionMessage.getSessionId(), 
                                "REACTION_ERROR", e.getMessage());
        }
    }
    
    /**
     * 广播表情反应更新
     */
    private void broadcastReactionUpdate(ReactionMessage reactionMessage, String userId) {
        try {
            // 设置广播消息的额外信息
            reactionMessage.setUserId(userId);
            reactionMessage.setTimestamp(System.currentTimeMillis());
            
            // 获取最新的反应统计（可选，减少请求次数可以省略）
            var reactions = messageService.getMessageReactions(reactionMessage.getMsgId());
            if (reactions.isSuccess() && reactions.getData() != null) {
                // 转换为广播消息格式
                var summaries = reactions.getData().stream()
                        .map(dto -> ReactionMessage.ReactionSummary.builder()
                                .emoji(dto.getEmoji())
                                .name(dto.getName())
                                .count(dto.getCount())
                                .userIds(dto.getUserIds())
                                .build())
                        .collect(java.util.stream.Collectors.toList());
                reactionMessage.setReactions(summaries);
            }
            
            // 向会话频道广播表情反应更新
            String destination = "/topic/reactions/" + reactionMessage.getSessionId();
            messagingTemplate.convertAndSend(destination, reactionMessage);
            
            log.debug("表情反应更新已广播: sessionId={}, msgId={}, emoji={}, action={}", 
                    reactionMessage.getSessionId(), reactionMessage.getMsgId(), 
                    reactionMessage.getEmoji(), reactionMessage.getAction());
                    
        } catch (Exception e) {
            log.error("广播表情反应更新失败: {}", e.getMessage(), e);
        }
    }

    private AckMessage createSuccessAck(String msgId, String sessionId) {
        AckMessage ackMessage = new AckMessage();
        ackMessage.setOriginalMsgId(msgId);
        ackMessage.setSessionId(sessionId);
        ackMessage.setStatus("SUCCESS");
        return ackMessage;
    }

    private AckMessage createErrorAck(String msgId, String sessionId, String errorCode, String errorMessage) {
        AckMessage ackMessage = new AckMessage();
        ackMessage.setOriginalMsgId(msgId);
        ackMessage.setSessionId(sessionId);
        ackMessage.setStatus("FAIL");
        ackMessage.setErrorCode(errorCode);
        ackMessage.setErrorMessage(errorMessage);
        return ackMessage;
    }
}
