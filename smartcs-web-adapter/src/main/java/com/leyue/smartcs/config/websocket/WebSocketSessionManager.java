package com.leyue.smartcs.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket会话管理器
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 本地会话缓存，用于快速查询，key为用户ID，value为会话ID
    private final Map<String, String> localUserSessionMap = new ConcurrentHashMap<>();
    
    // Redis中存储会话的键前缀
    private static final String REDIS_SESSION_PREFIX = "ws:session:";
    private static final String REDIS_AGENT_SET = "ws:agents";
    private static final String REDIS_CUSTOMER_SET = "ws:customers";
    private static final long SESSION_EXPIRE_SECONDS = 24 * 60 * 60; // 会话过期时间，24小时

    @Autowired
    public WebSocketSessionManager(SimpMessagingTemplate messagingTemplate, 
                                  RedisTemplate<String, Object> redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 注册用户会话
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     * @param userType 用户类型，CUSTOMER或AGENT
     */
    public void registerSession(String userId, String sessionId, String userType) {
        log.info("注册WebSocket会话: userId={}, sessionId={}, userType={}", userId, sessionId, userType);
        
        // 存储本地缓存
        localUserSessionMap.put(userId, sessionId);
        
        // 存储到Redis，使用Hash结构存储会话信息
        String sessionKey = REDIS_SESSION_PREFIX + userId;
        redisTemplate.opsForHash().put(sessionKey, "sessionId", sessionId);
        redisTemplate.opsForHash().put(sessionKey, "userType", userType);
        redisTemplate.expire(sessionKey, SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        // 根据用户类型添加到相应的集合
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            redisTemplate.opsForSet().add(REDIS_CUSTOMER_SET, userId);
        } else if ("AGENT".equalsIgnoreCase(userType)) {
            redisTemplate.opsForSet().add(REDIS_AGENT_SET, userId);
        }
    }

    /**
     * 移除用户会话
     * @param userId 用户ID
     */
    public void removeSession(String userId) {
        log.info("移除WebSocket会话: userId={}", userId);
        
        // 获取用户类型
        String userType = (String) redisTemplate.opsForHash().get(REDIS_SESSION_PREFIX + userId, "userType");
        
        // 从本地缓存移除
        localUserSessionMap.remove(userId);
        
        // 从Redis移除
        redisTemplate.delete(REDIS_SESSION_PREFIX + userId);
        
        // 从用户集合中移除
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            redisTemplate.opsForSet().remove(REDIS_CUSTOMER_SET, userId);
        } else if ("AGENT".equalsIgnoreCase(userType)) {
            redisTemplate.opsForSet().remove(REDIS_AGENT_SET, userId);
        }
    }

    /**
     * 处理断开连接事件
     * @param event 断开连接事件
     */
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket断开连接: sessionId={}", sessionId);
        
        // 遍历本地缓存，找到对应的用户ID
        for (Map.Entry<String, String> entry : localUserSessionMap.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                removeSession(entry.getKey());
                break;
            }
        }
    }

    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param destination 目的地
     * @param payload 消息内容
     */
    public void sendToUser(String userId, String destination, Object payload) {
        log.debug("发送消息给用户: userId={}, destination={}", userId, destination);
        
        // 构建目的地，例如 /user/{userId}/queue/messages
        String userDestination = "/queue/" + destination;
        
        // 创建头信息
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setLeaveMutable(true);
        
        // 发送消息
        messagingTemplate.convertAndSendToUser(userId, userDestination, payload, headerAccessor.getMessageHeaders());
    }

    /**
     * 获取所有在线客服
     * @return 客服ID集合
     */
    public Set<Object> getAllOnlineAgents() {
        return redisTemplate.opsForSet().members(REDIS_AGENT_SET);
    }

    /**
     * 获取所有在线客户
     * @return 客户ID集合
     */
    public Set<Object> getAllOnlineCustomers() {
        return redisTemplate.opsForSet().members(REDIS_CUSTOMER_SET);
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        return redisTemplate.hasKey(REDIS_SESSION_PREFIX + userId);
    }
}
