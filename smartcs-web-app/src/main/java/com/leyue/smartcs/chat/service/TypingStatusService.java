package com.leyue.smartcs.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 输入状态管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TypingStatusService {
    
    private final StringRedisTemplate redisTemplate;
    
    // Redis键前缀
    private static final String TYPING_KEY_PREFIX = "chat:typing:session:";
    // 输入状态过期时间（秒）
    private static final long TYPING_TIMEOUT = 5;
    
    /**
     * 设置用户正在输入状态
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    public void setUserTyping(Long sessionId, String userId) {
        String key = TYPING_KEY_PREFIX + sessionId;
        redisTemplate.opsForSet().add(key, userId);
        redisTemplate.expire(key, Duration.ofSeconds(TYPING_TIMEOUT));
        
        log.debug("用户 {} 开始输入，会话 {}", userId, sessionId);
    }
    
    /**
     * 移除用户输入状态
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    public void removeUserTyping(Long sessionId, String userId) {
        String key = TYPING_KEY_PREFIX + sessionId;
        redisTemplate.opsForSet().remove(key, userId);
        
        log.debug("用户 {} 停止输入，会话 {}", userId, sessionId);
    }
    
    /**
     * 获取会话中正在输入的用户列表
     *
     * @param sessionId 会话ID
     * @return 正在输入的用户ID集合
     */
    public Set<String> getTypingUsers(Long sessionId) {
        String key = TYPING_KEY_PREFIX + sessionId;
        return redisTemplate.opsForSet().members(key);
    }
    
    /**
     * 检查用户是否正在输入
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否正在输入
     */
    public boolean isUserTyping(Long sessionId, String userId) {
        String key = TYPING_KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userId));
    }
    
    /**
     * 清理会话的所有输入状态
     *
     * @param sessionId 会话ID
     */
    public void clearSessionTyping(Long sessionId) {
        String key = TYPING_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        
        log.debug("清理会话 {} 的所有输入状态", sessionId);
    }
    
    /**
     * 刷新用户输入状态的过期时间
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    public void refreshUserTyping(Long sessionId, String userId) {
        if (isUserTyping(sessionId, userId)) {
            setUserTyping(sessionId, userId);
        }
    }
}