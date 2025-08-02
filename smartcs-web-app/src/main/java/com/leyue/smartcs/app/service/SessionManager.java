package com.leyue.smartcs.app.service;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.app.entity.AppTestSession;
import com.leyue.smartcs.domain.app.gateway.AppTestSessionGateway;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话管理服务
 * 负责会话的创建、管理和生命周期控制
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManager {

    private final AppTestSessionGateway appTestSessionGateway;

    /**
     * 生成会话ID
     */
    public String generateSessionId(Long appId, String providedSessionId) {
        if (providedSessionId != null && !providedSessionId.trim().isEmpty()) {
            return providedSessionId;
        }
        return "app_" + appId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 初始化或获取测试会话
     */
    public AppTestSession initializeSession(String sessionId, Long appId, Long modelId, AiAppChatCmd cmd) {
        try {
            // 尝试获取现有会话
            AppTestSession existingSession = appTestSessionGateway.findBySessionId(sessionId);
            if (existingSession != null && existingSession.isActive()) {
                log.info("使用现有会话: sessionId={}", sessionId);
                return existingSession;
            }
            
            // 构建会话配置，包含完整的聊天参数
            Map<String, Object> sessionConfig = buildSessionConfig(modelId, cmd);
            
            // 创建新会话
            AppTestSession newSession = AppTestSession.createNew(sessionId, appId, modelId, null, sessionConfig);
            appTestSessionGateway.save(newSession);
            log.info("创建新测试会话: sessionId={}, appId={}, modelId={}", sessionId, appId, modelId);
            return newSession;
            
        } catch (Exception e) {
            log.error("初始化测试会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new BizException("初始化测试会话失败: " + e.getMessage());
        }
    }

    /**
     * 构建会话配置
     */
    private Map<String, Object> buildSessionConfig(Long modelId, AiAppChatCmd cmd) {
        Map<String, Object> sessionConfig = new HashMap<>();
        sessionConfig.put("modelId", modelId);
        sessionConfig.put("includeHistory", cmd.getIncludeHistory());
        sessionConfig.put("enableRAG", cmd.getEnableRAG());
        
        if (cmd.getKnowledgeId() != null) {
            sessionConfig.put("knowledgeId", cmd.getKnowledgeId());
        }
        if (cmd.getInferenceParams() != null) {
            sessionConfig.put("inferenceParams", cmd.getInferenceParams());
        }
        
        sessionConfig.put("timeout", cmd.getTimeout());
        
        if (cmd.getVariables() != null) {
            sessionConfig.put("variables", cmd.getVariables());
        }
        
        return sessionConfig;
    }

    /**
     * 更新会话状态
     */
    public void updateSessionStatus(String sessionId, AppTestSession.SessionState status) {
        try {
            AppTestSession session = appTestSessionGateway.findBySessionId(sessionId);
            if (session != null) {
                session.updateStatus(status);
                appTestSessionGateway.save(session);
                log.info("更新会话状态: sessionId={}, status={}", sessionId, status);
            }
        } catch (Exception e) {
            log.error("更新会话状态失败: sessionId={}, status={}, error={}", 
                     sessionId, status, e.getMessage(), e);
        }
    }

    /**
     * 关闭会话
     */
    public void closeSession(String sessionId) {
        try {
            AppTestSession session = appTestSessionGateway.findBySessionId(sessionId);
            if (session != null && session.isActive()) {
                session.updateStatus(AppTestSession.SessionState.FINISHED);
                appTestSessionGateway.save(session);
                log.info("关闭会话: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("关闭会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 检查会话是否存在且活跃
     */
    public boolean isSessionActive(String sessionId) {
        try {
            AppTestSession session = appTestSessionGateway.findBySessionId(sessionId);
            return session != null && session.isActive();
        } catch (Exception e) {
            log.error("检查会话状态失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取会话信息
     */
    public AppTestSession getSession(String sessionId) {
        try {
            return appTestSessionGateway.findBySessionId(sessionId);
        } catch (Exception e) {
            log.error("获取会话信息失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 批量清理过期会话
     */
    public void cleanupExpiredSessions(long expiredTimeMillis) {
        try {
            long cutoffTime = System.currentTimeMillis() - expiredTimeMillis;
            // 这里可以实现批量清理逻辑
            log.info("开始清理过期会话: cutoffTime={}", cutoffTime);
            // TODO: 实现批量清理过期会话的逻辑
        } catch (Exception e) {
            log.error("清理过期会话失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 获取会话统计信息
     */
    public SessionStats getSessionStats(String sessionId) {
        try {
            AppTestSession session = appTestSessionGateway.findBySessionId(sessionId);
            if (session != null) {
                return new SessionStats(
                    sessionId,
                    session.getStatus(),
                    session.getCreatedAt(),
                    session.getUpdatedAt(),
                    session.isActive()
                );
            }
            return null;
        } catch (Exception e) {
            log.error("获取会话统计失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return null;
        }
    }

        /**
     * 会话统计信息
     */
    public static class SessionStats {
        public final String sessionId;
        public final AppTestSession.SessionState status;
        public final long createdAt;
        public final long updatedAt;
        public final boolean active;

        public SessionStats(String sessionId, AppTestSession.SessionState status, 
                          long createdAt, long updatedAt, boolean active) {
            this.sessionId = sessionId;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.active = active;
        }
        
        @Override
        public String toString() {
            return String.format("SessionStats{sessionId='%s', status=%s, createdAt=%d, updatedAt=%d, active=%s}", 
                               sessionId, status, createdAt, updatedAt, active);
        }
    }
}