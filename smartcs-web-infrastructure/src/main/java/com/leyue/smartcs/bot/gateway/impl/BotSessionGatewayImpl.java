package com.leyue.smartcs.bot.gateway.impl;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.domain.bot.gateway.SessionGateway;
import com.leyue.smartcs.domain.bot.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 会话网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BotSessionGatewayImpl implements SessionGateway {
    
    private final RedissonClient redissonClient;
    
    /**
     * 对话缓存前缀
     */
    private static final String CONVERSATION_PREFIX = "bot:conversation:";
    
    /**
     * 用户信息缓存前缀
     */
    private static final String USER_INFO_PREFIX = "session:user:";
    
    /**
     * 对话缓存过期时间（小时）
     */
    private static final int CONVERSATION_EXPIRE_HOURS = 24;
    
    @Override
    public boolean saveConversation(Conversation conversation) {
        try {
            String key = CONVERSATION_PREFIX + conversation.getSessionId();
            RBucket<String> bucket = redissonClient.getBucket(key);
            bucket.set(JSON.toJSONString(conversation), CONVERSATION_EXPIRE_HOURS, TimeUnit.HOURS);
            return true;
        } catch (Exception e) {
            log.error("保存对话失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<Conversation> getConversation(String sessionId) {
        try {
            String key = CONVERSATION_PREFIX + sessionId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            String json = bucket.get();
            
            if (json != null) {
                Conversation conversation = JSON.parseObject(json, Conversation.class);
                return Optional.of(conversation);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("获取对话失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean deleteConversation(String sessionId) {
        try {
            String key = CONVERSATION_PREFIX + sessionId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            return bucket.delete();
        } catch (Exception e) {
            log.error("删除对话失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<String> getUserInfo(String sessionId) {
        try {
            String key = USER_INFO_PREFIX + sessionId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            String userInfo = bucket.get();
            return Optional.ofNullable(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
} 