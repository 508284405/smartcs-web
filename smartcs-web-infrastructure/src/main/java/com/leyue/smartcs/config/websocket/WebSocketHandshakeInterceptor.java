package com.leyue.smartcs.config.websocket;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.api.UserService;
import com.leyue.smartcs.config.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            // 1. 从 URL 参数获取 token
            String token = httpRequest.getParameter("token");
            if(token == null){
                log.info("WebSocket握手失败，token为空");
                return false;
            }
            if (!userService.validateUserToken(token)) {
                // 验证失败
                log.info("WebSocket握手失败，token验证失败");
                return false;
            }
            UserContext.UserInfo currentUser = UserContext.getCurrentUser();
            // 角色编码USER=usertype(0),AGENT=usertype(1)
            Integer userType = currentUser.getRoles().stream().map(role -> {
                if (role.getRoleCode().equals("USER")) {
                    return 0;
                } else if (role.getRoleCode().equals("AGENT")) {
                    return 1;
                }
                return null;
            }).findAny().orElseThrow(() -> new BizException("角色不支持"));
            // 将 userId 和 userType 放入 attributes，供后续 CustomHandshakeHandler 绑定 Principal 使用
            attributes.put("userId", currentUser.getId());
            attributes.put("userType", userType);
            return true;
        }
        log.warn("WebSocket握手失败，token无效");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }
} 