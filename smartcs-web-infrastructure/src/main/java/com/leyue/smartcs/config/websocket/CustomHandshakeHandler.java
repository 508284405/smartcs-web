package com.leyue.smartcs.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 自定义握手处理器，将 userId 作为 Principal 绑定到 WebSocket session
 * 这样后端 convertAndSendToUser(userId, ...) 能正确推送到指定用户
 */
@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        Object userId = attributes.get("userId");
        if (userId != null) {
            // 返回以 userId 为 name 的 Principal，确保点对点推送时 userId 能被正确识别
            return userId::toString;
        }
        return super.determineUser(request, wsHandler, attributes);
    }
} 