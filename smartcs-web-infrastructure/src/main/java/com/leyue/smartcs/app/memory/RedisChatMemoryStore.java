package com.leyue.smartcs.app.memory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的聊天记忆存储实现
 * 支持分布式会话管理和持久化存储
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String MEMORY_KEY_PREFIX = "chat:memory:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24); // 24小时过期
    private static final int MAX_MESSAGES_PER_SESSION = 50; // 每个会话最多保存50条消息

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = buildMemoryKey(memoryId);
        try {
            List<Object> messageList = redisTemplate.opsForList().range(key, 0, -1);
            if (messageList == null || messageList.isEmpty()) {
                log.debug("会话记忆为空: memoryId={}", memoryId);
                return new ArrayList<>();
            }

            List<ChatMessage> messages = new ArrayList<>();
            for (Object messageObj : messageList) {
                ChatMessage message = deserializeChatMessage(messageObj.toString());
                if (message != null) {
                    messages.add(message);
                }
            }
            
            log.debug("加载会话记忆: memoryId={}, messageCount={}", memoryId, messages.size());
            return messages;
            
        } catch (Exception e) {
            log.error("获取聊天记忆失败: memoryId={}, error={}", memoryId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = buildMemoryKey(memoryId);
        try {
            // 清除旧的消息
            redisTemplate.delete(key);
            
            if (messages == null || messages.isEmpty()) {
                log.debug("清空会话记忆: memoryId={}", memoryId);
                return;
            }

            // 限制消息数量，保留最新的消息
            List<ChatMessage> limitedMessages = messages;
            if (messages.size() > MAX_MESSAGES_PER_SESSION) {
                limitedMessages = messages.subList(
                    messages.size() - MAX_MESSAGES_PER_SESSION, 
                    messages.size()
                );
                log.info("会话记忆超过限制，保留最新{}条消息: memoryId={}", MAX_MESSAGES_PER_SESSION, memoryId);
            }

            // 序列化并存储消息
            List<String> serializedMessages = new ArrayList<>();
            for (ChatMessage message : limitedMessages) {
                String serialized = serializeChatMessage(message);
                if (serialized != null) {
                    serializedMessages.add(serialized);
                }
            }

            if (!serializedMessages.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, serializedMessages.toArray());
                redisTemplate.expire(key, DEFAULT_TTL.toMillis(), TimeUnit.MILLISECONDS);
                log.debug("更新会话记忆: memoryId={}, messageCount={}", memoryId, serializedMessages.size());
            }
            
        } catch (Exception e) {
            log.error("更新聊天记忆失败: memoryId={}, error={}", memoryId, e.getMessage(), e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = buildMemoryKey(memoryId);
        try {
            redisTemplate.delete(key);
            log.info("删除会话记忆: memoryId={}", memoryId);
        } catch (Exception e) {
            log.error("删除聊天记忆失败: memoryId={}, error={}", memoryId, e.getMessage(), e);
        }
    }

    /**
     * 构建记忆存储的Redis键
     */
    private String buildMemoryKey(Object memoryId) {
        return MEMORY_KEY_PREFIX + memoryId.toString();
    }

    /**
     * 序列化聊天消息
     */
    private String serializeChatMessage(ChatMessage message) {
        try {
            Map<String, Object> messageMap = Map.of(
                "type", message.type().name(),
                "text", message.text() != null ? message.text() : ""
            );
            return JSON.toJSONString(messageMap);
        } catch (Exception e) {
            log.error("序列化聊天消息失败", e);
            return null;
        }
    }

    /**
     * 反序列化聊天消息
     */
    private ChatMessage deserializeChatMessage(String serialized) {
        try {
            Map<String, Object> messageMap = JSON.parseObject(serialized, new TypeReference<Map<String, Object>>() {});
            String type = (String) messageMap.get("type");
            String text = (String) messageMap.get("text");

            return switch (type) {
                case "SYSTEM" -> SystemMessage.from(text);
                case "USER" -> UserMessage.from(text);
                case "AI" -> AiMessage.from(text);
                default -> {
                    log.warn("未知的消息类型: {}", type);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("反序列化聊天消息失败: serialized={}", serialized, e);
            return null;
        }
    }

    /**
     * 获取会话记忆统计信息
     */
    public long getMessageCount(Object memoryId) {
        String key = buildMemoryKey(memoryId);
        try {
            Long count = redisTemplate.opsForList().size(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取会话记忆统计失败: memoryId={}", memoryId, e);
            return 0;
        }
    }

    /**
     * 设置会话记忆过期时间
     */
    public void setMemoryTtl(Object memoryId, Duration ttl) {
        String key = buildMemoryKey(memoryId);
        try {
            redisTemplate.expire(key, ttl.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("设置会话记忆过期时间: memoryId={}, ttl={}", memoryId, ttl);
        } catch (Exception e) {
            log.error("设置会话记忆过期时间失败: memoryId={}", memoryId, e);
        }
    }

    /**
     * 清理过期的会话记忆
     */
    public void cleanExpiredMemories() {
        // 由Redis自动清理过期键，这里主要用于监控和日志
        log.info("Redis会自动清理过期的会话记忆");
    }
}