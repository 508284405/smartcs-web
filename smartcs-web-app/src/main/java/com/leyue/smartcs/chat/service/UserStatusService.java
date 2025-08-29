package com.leyue.smartcs.chat.service;

import com.leyue.smartcs.domain.chat.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户状态管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusService {
    
    private final StringRedisTemplate redisTemplate;
    
    // Redis键前缀
    private static final String USER_STATUS_PREFIX = "chat:user:status:";
    private static final String USER_LAST_SEEN_PREFIX = "chat:user:last_seen:";
    private static final String ONLINE_USERS_SET = "chat:online:users";
    
    // 在线状态过期时间（秒）
    private static final long ONLINE_TIMEOUT = 300; // 5分钟
    
    /**
     * 设置用户在线状态
     *
     * @param userId 用户ID
     * @param status 用户状态
     * @param statusMessage 状态消息（可选）
     */
    public void setUserStatus(String userId, UserStatus status, String statusMessage) {
        String statusKey = USER_STATUS_PREFIX + userId;
        String lastSeenKey = USER_LAST_SEEN_PREFIX + userId;
        long currentTime = System.currentTimeMillis();
        
        // 设置用户状态
        Map<String, String> statusData = new HashMap<>();
        statusData.put("status", status.getCode());
        statusData.put("lastSeenAt", String.valueOf(currentTime));
        if (statusMessage != null) {
            statusData.put("statusMessage", statusMessage);
        }
        
        redisTemplate.opsForHash().putAll(statusKey, statusData);
        
        // 如果是在线状态，添加到在线用户集合并设置过期时间
        if (status == UserStatus.ONLINE) {
            redisTemplate.opsForSet().add(ONLINE_USERS_SET, userId);
            redisTemplate.expire(statusKey, Duration.ofSeconds(ONLINE_TIMEOUT));
        } else {
            // 非在线状态，从在线用户集合中移除
            redisTemplate.opsForSet().remove(ONLINE_USERS_SET, userId);
        }
        
        // 更新最后在线时间
        redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(currentTime));
        
        log.debug("用户状态更新: userId={}, status={}", userId, status.getCode());
    }
    
    /**
     * 获取用户当前状态
     *
     * @param userId 用户ID
     * @return 用户状态信息
     */
    public Map<String, Object> getUserStatus(String userId) {
        String statusKey = USER_STATUS_PREFIX + userId;
        Map<Object, Object> statusData = redisTemplate.opsForHash().entries(statusKey);
        
        Map<String, Object> result = new HashMap<>();
        if (statusData.isEmpty()) {
            // 用户没有状态信息，默认离线
            result.put("status", UserStatus.OFFLINE.getCode());
            result.put("lastSeenAt", 0L);
        } else {
            result.put("status", statusData.getOrDefault("status", UserStatus.OFFLINE.getCode()));
            result.put("lastSeenAt", Long.parseLong((String) statusData.getOrDefault("lastSeenAt", "0")));
            if (statusData.containsKey("statusMessage")) {
                result.put("statusMessage", statusData.get("statusMessage"));
            }
        }
        
        return result;
    }
    
    /**
     * 获取所有在线用户
     *
     * @return 在线用户ID集合
     */
    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_SET);
    }
    
    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_SET, userId));
    }
    
    /**
     * 用户上线
     *
     * @param userId 用户ID
     */
    public void userOnline(String userId) {
        setUserStatus(userId, UserStatus.ONLINE, null);
    }
    
    /**
     * 用户离线
     *
     * @param userId 用户ID
     */
    public void userOffline(String userId) {
        setUserStatus(userId, UserStatus.OFFLINE, null);
    }
    
    /**
     * 刷新用户在线状态（心跳）
     *
     * @param userId 用户ID
     */
    public void refreshUserOnline(String userId) {
        if (isUserOnline(userId)) {
            String statusKey = USER_STATUS_PREFIX + userId;
            redisTemplate.expire(statusKey, Duration.ofSeconds(ONLINE_TIMEOUT));
            
            // 更新最后在线时间
            String lastSeenKey = USER_LAST_SEEN_PREFIX + userId;
            redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(System.currentTimeMillis()));
        }
    }
    
    /**
     * 清理用户状态
     *
     * @param userId 用户ID
     */
    public void clearUserStatus(String userId) {
        String statusKey = USER_STATUS_PREFIX + userId;
        String lastSeenKey = USER_LAST_SEEN_PREFIX + userId;
        
        redisTemplate.delete(statusKey);
        redisTemplate.delete(lastSeenKey);
        redisTemplate.opsForSet().remove(ONLINE_USERS_SET, userId);
        
        log.debug("清理用户状态: userId={}", userId);
    }
}