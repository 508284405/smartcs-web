package com.leyue.smartcs.rag.persistence.impl;

import com.leyue.smartcs.rag.persistence.PersistenceManager;
import com.leyue.smartcs.rag.persistence.model.MessageEntity;
import com.leyue.smartcs.rag.persistence.model.MessageQuery;
import com.leyue.smartcs.rag.persistence.model.SessionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库持久化管理器实现
 * 默认的简单实现，后续可以集成实际的数据库操作
 */
@Service
@Slf4j
public class DatabasePersistenceManager implements PersistenceManager {

    // 临时存储，实际实现应该使用数据库
    private final ConcurrentHashMap<String, MessageEntity> messages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SessionEntity> sessions = new ConcurrentHashMap<>();

    @Override
    public void persistMessage(MessageEntity message) {
        try {
            messages.put(message.getId(), message);
            log.debug("持久化消息: messageId={}, sessionId={}", message.getId(), message.getSessionId());
        } catch (Exception e) {
            log.error("持久化消息失败: messageId={}, error={}", message.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void persistMessages(List<MessageEntity> messagesList) {
        for (MessageEntity message : messagesList) {
            persistMessage(message);
        }
    }

    @Override
    public void persistSession(SessionEntity session) {
        try {
            sessions.put(session.getId(), session);
            log.debug("持久化会话: sessionId={}", session.getId());
        } catch (Exception e) {
            log.error("持久化会话失败: sessionId={}, error={}", session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public List<MessageEntity> queryMessages(MessageQuery query) {
        List<MessageEntity> result = new ArrayList<>();
        try {
            for (MessageEntity message : messages.values()) {
                if (matchesQuery(message, query)) {
                    result.add(message);
                }
            }
            log.debug("查询消息: sessionId={}, resultCount={}", query.getSessionId(), result.size());
        } catch (Exception e) {
            log.error("查询消息失败: error={}", e.getMessage(), e);
        }
        return result;
    }

    @Override
    public SessionEntity getSession(String sessionId) {
        try {
            SessionEntity session = sessions.get(sessionId);
            log.debug("获取会话: sessionId={}, found={}", sessionId, session != null);
            return session;
        } catch (Exception e) {
            log.error("获取会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        try {
            SessionEntity session = sessions.get(sessionId);
            if (session != null) {
                session.setStatus(status);
                session.setUpdatedAt(java.time.LocalDateTime.now());
                log.debug("更新会话状态: sessionId={}, status={}", sessionId, status);
            }
        } catch (Exception e) {
            log.error("更新会话状态失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void deleteSession(String sessionId) {
        try {
            sessions.remove(sessionId);
            // 删除相关消息
            messages.entrySet().removeIf(entry -> sessionId.equals(entry.getValue().getSessionId()));
            log.debug("删除会话: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("删除会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public long getMessageCount(String sessionId) {
        try {
            long count = messages.values().stream()
                    .filter(message -> sessionId.equals(message.getSessionId()))
                    .count();
            log.debug("获取会话消息数量: sessionId={}, count={}", sessionId, count);
            return count;
        } catch (Exception e) {
            log.error("获取消息数量失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<SessionEntity> getUserSessions(String userId, int limit) {
        List<SessionEntity> result = new ArrayList<>();
        try {
            sessions.values().stream()
                    .filter(session -> userId.equals(session.getUserId()))
                    .limit(limit)
                    .forEach(result::add);
            log.debug("获取用户会话: userId={}, limit={}, resultCount={}", userId, limit, result.size());
        } catch (Exception e) {
            log.error("获取用户会话失败: userId={}, error={}", userId, e.getMessage(), e);
        }
        return result;
    }

    @Override
    public int cleanupExpiredData(long beforeTimestamp) {
        int cleaned = 0;
        try {
            java.time.LocalDateTime cutoffTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(beforeTimestamp),
                    java.time.ZoneId.systemDefault()
            );

            // 清理过期会话
            List<String> expiredSessions = new ArrayList<>();
            for (SessionEntity session : sessions.values()) {
                if (session.getUpdatedAt() != null && session.getUpdatedAt().isBefore(cutoffTime)) {
                    expiredSessions.add(session.getId());
                }
            }

            for (String sessionId : expiredSessions) {
                deleteSession(sessionId);
                cleaned++;
            }

            log.info("清理过期数据: beforeTimestamp={}, cleanedCount={}", beforeTimestamp, cleaned);
        } catch (Exception e) {
            log.error("清理过期数据失败: error={}", e.getMessage(), e);
        }
        return cleaned;
    }

    @Override
    public boolean messageExists(String messageId) {
        return messages.containsKey(messageId);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * 检查消息是否匹配查询条件
     */
    private boolean matchesQuery(MessageEntity message, MessageQuery query) {
        if (query.getSessionId() != null && !query.getSessionId().equals(message.getSessionId())) {
            return false;
        }
        if (query.getUserId() != null && !query.getUserId().equals(message.getUserId())) {
            return false;
        }
        if (query.getAppId() != null && !query.getAppId().equals(message.getAppId())) {
            return false;
        }
        if (query.getRoles() != null && !query.getRoles().contains(message.getRole())) {
            return false;
        }
        if (query.getTypes() != null && !query.getTypes().contains(message.getType())) {
            return false;
        }
        return true;
    }
}