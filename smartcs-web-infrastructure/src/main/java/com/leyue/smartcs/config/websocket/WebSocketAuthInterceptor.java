package com.leyue.smartcs.config.websocket;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * WebSocket认证拦截器
 */
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 处理连接请求
            String token = accessor.getFirstNativeHeader("Authorization");
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                // 验证JWT令牌并设置认证信息（此处简化，实际应使用JWT验证）
                String userId = validateToken(jwtToken);
                
                if (userId != null) {
                    // 设置用户认证信息
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    accessor.setUser(authentication);
                    
                    // 将用户ID存储到会话属性中，方便后续处理
                    accessor.getSessionAttributes().put("userId", userId);
                    
                    // 从连接头获取用户类型（客户或客服）
                    String userType = accessor.getFirstNativeHeader("UserType");
                    if (StringUtils.hasText(userType)) {
                        accessor.getSessionAttributes().put("userType", userType);
                    }
                }
            }
        }
        
        return message;
    }
    
    /**
     * 验证JWT令牌并提取用户ID
     * 注意：这里是简化实现，实际项目应使用专门的JWT验证服务
     */
    private String validateToken(String token) {
        // 简化处理，实际应使用JWT库验证令牌
        try {
            // 实际项目中应该使用JWT库解析令牌并验证签名
            // 例如：Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            // 然后从claims中获取用户ID：String userId = claims.getSubject();
            
            // 这里简化处理，假设令牌就是用户ID
            return token;
        } catch (Exception e) {
            return null;
        }
    }
}
