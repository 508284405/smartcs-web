package com.leyue.smartcs.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 注册STOMP端点，客户连接WebSocket的入口
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")  // 允许跨域
                .withSockJS();  // 启用SockJS支持
        
        // 客服端连接入口
        registry.addEndpoint("/ws/agent")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // 配置消息代理，用于客户端订阅
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 配置应用程序前缀，用于客户端发送消息到服务端
        registry.setApplicationDestinationPrefixes("/app");
        
        // 配置用户目的地前缀，用于点对点通信
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // 配置客户端入站通道，添加拦截器
        registration.interceptors(webSocketAuthInterceptor());
    }
    
    /**
     * WebSocket认证拦截器
     */
    public WebSocketAuthInterceptor webSocketAuthInterceptor() {
        return new WebSocketAuthInterceptor();
    }
}
