package com.leyue.smartcs.config.websocket;

import com.leyue.smartcs.api.chat.dto.websocket.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public WebSocketEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 处理WebSocket连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket连接成功: sessionId={}", sessionId);
        
        // 获取会话属性中的用户信息
        String userId = null;
        String userType = null;
        
        // 安全获取会话属性
        java.util.Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object userIdObj = sessionAttributes.get("userId");
            Object userTypeObj = sessionAttributes.get("userType");
            
            if (userIdObj != null) {
                userId = userIdObj.toString();
            }
            
            if (userTypeObj != null) {
                userType = userTypeObj.toString();
            }
        }
        
        if (userId != null && userType != null) {
            // 注册会话
            sessionManager.registerSession(userId, sessionId, userType);
            
            // 发送连接成功通知
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setCode("CONNECT_SUCCESS");
            systemMessage.setContent("连接成功");
            sessionManager.sendToUser(userId.toString(), "notifications", systemMessage);
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
