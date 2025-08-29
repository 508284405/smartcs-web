package com.leyue.smartcs.config.websocket;

import com.leyue.smartcs.api.OfflineMessageService;
import com.leyue.smartcs.chat.service.UserStatusService;
import com.leyue.smartcs.dto.chat.ws.SystemMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * WebSocket事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;
    private final OfflineMessageService offlineMessageService;
    private final UserStatusService userStatusService;

    /**
     * 处理WebSocket连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.debug("WebSocket连接成功: sessionId={}", sessionId);
        String userId = event.getUser().getName();

        if (userId != null) {
            // 发送连接成功通知
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setCode("CONNECT_SUCCESS");
            systemMessage.setContent("连接成功");
            sessionManager.sendToUser(userId, "messages", systemMessage);
            
            // 用户上线状态更新
            try {
                userStatusService.userOnline(userId);
                log.debug("用户上线状态已更新: userId={}", userId);
            } catch (Exception e) {
                log.error("更新用户上线状态失败: userId={}", userId, e);
            }
            
            // 处理用户上线，推送离线消息摘要
            try {
                Long userIdLong = Long.valueOf(userId);
                offlineMessageService.processUserOnline(userIdLong);
            } catch (Exception e) {
                log.error("处理用户上线离线消息失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 处理WebSocket断开连接事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event == null || event.getMessage() == null) {
            log.warn("WebSocket断开连接事件或消息为空");
            return;
        }

        // 获取会话的头部信息
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket断开连接: sessionId={}", sessionId);

        // 获取用户ID（如果可能的话）
        try {
            if (event.getUser() != null) {
                String userId = event.getUser().getName();
                if (userId != null) {
                    // 用户离线状态更新
                    userStatusService.userOffline(userId);
                    log.debug("用户离线状态已更新: userId={}", userId);
                }
            }
        } catch (Exception e) {
            log.error("更新用户离线状态失败: sessionId={}", sessionId, e);
        }

        // 处理断开连接
        sessionManager.handleDisconnectEvent(event);
    }

    /**
     * 处理WebSocket订阅事件
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        log.info("WebSocket订阅: sessionId={}, destination={}", sessionId, destination);
    }
}
