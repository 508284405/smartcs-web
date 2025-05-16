package com.leyue.smartcs.websocket;

import com.leyue.smartcs.dto.chat.ws.AckMessage;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import com.leyue.smartcs.dto.chat.ws.WebSocketMessage;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.api.MessageSendService;
import com.leyue.smartcs.api.MessageValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
        chatMessage.setFromUserId(String.valueOf(userId));
        chatMessage.setCreateTime(System.currentTimeMillis());

        // 检查用户类型，从会话属性中获取
        if (headerAccessor.getSessionAttributes() != null) {
            Object userType = headerAccessor.getSessionAttributes().get("userType");
            if (userType != null) {
                chatMessage.setFromUserType(userType.toString());
            }
        }

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
}
