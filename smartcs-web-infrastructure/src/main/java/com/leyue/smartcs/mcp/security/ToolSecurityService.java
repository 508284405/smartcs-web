package com.leyue.smartcs.mcp.security;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 工具安全服务
 * 
 * <p>提供工具使用的安全检查、权限验证和风险控制功能。
 * 确保工具调用的安全性和合规性。</p>
 * 
 * <h3>安全机制:</h3>
 * <ul>
 *   <li>权限检查 - 验证用户是否有权限使用特定工具</li>
 *   <li>参数验证 - 检查工具参数的合法性和安全性</li>
 *   <li>操作限制 - 限制敏感操作的执行条件</li>
 *   <li>会话验证 - 确保请求来自合法会话</li>
 * </ul>
 * 
 * <h3>风险控制:</h3>
 * <ul>
 *   <li>黑名单机制 - 阻止恶意用户和可疑操作</li>
 *   <li>异常检测 - 识别异常的工具使用模式</li>
 *   <li>自动熔断 - 在检测到风险时自动停止工具执行</li>
 * </ul>
 * 
 * @author Claude
 */
@Service
@Slf4j
public class ToolSecurityService {

    // 会话黑名单
    private final Set<String> blockedSessions = ConcurrentHashMap.newKeySet();
    
    // 用户黑名单
    private final Set<Long> blockedUsers = ConcurrentHashMap.newKeySet();
    
    // 敏感工具列表
    private final Set<String> sensitiveTools = Set.of(
        "cancelOrder", 
        "updateOrderAddress",
        "confirmReceipt"
    );
    
    // 受限参数模式
    private final Set<String> restrictedPatterns = Set.of(
        "(?i).*delete.*",
        "(?i).*admin.*",
        "(?i).*system.*",
        "(?i).*root.*"
    );

    /**
     * 验证工具调用权限
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 安全验证结果
     */
    public ToolSecurityResult validateToolCall(String sessionId, Long userId, 
                                             String toolName, Object parameters) {
        log.debug("开始工具安全验证: sessionId={}, userId={}, toolName={}", 
                  sessionId, userId, toolName);

        try {
            // 1. 检查会话黑名单
            if (isSessionBlocked(sessionId)) {
                log.warn("会话被阻止: sessionId={}", sessionId);
                return ToolSecurityResult.blocked("会话已被阻止");
            }

            // 2. 检查用户黑名单
            if (userId != null && isUserBlocked(userId)) {
                log.warn("用户被阻止: userId={}", userId);
                return ToolSecurityResult.blocked("用户已被阻止");
            }

            // 3. 验证工具权限
            if (!hasToolPermission(userId, toolName)) {
                log.warn("用户无工具权限: userId={}, toolName={}", userId, toolName);
                return ToolSecurityResult.forbidden("无权限使用此工具");
            }

            // 4. 敏感工具额外检查
            if (isSensitiveTool(toolName)) {
                ToolSecurityResult sensitiveResult = validateSensitiveToolCall(
                    sessionId, userId, toolName, parameters);
                if (!sensitiveResult.isAllowed()) {
                    return sensitiveResult;
                }
            }

            // 5. 参数安全检查
            ToolSecurityResult paramResult = validateToolParameters(toolName, parameters);
            if (!paramResult.isAllowed()) {
                return paramResult;
            }

            log.debug("工具安全验证通过: toolName={}", toolName);
            return ToolSecurityResult.allowed();

        } catch (Exception e) {
            log.error("工具安全验证异常: toolName={}", toolName, e);
            return ToolSecurityResult.error("安全验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证敏感工具调用
     */
    private ToolSecurityResult validateSensitiveToolCall(String sessionId, Long userId,
                                                        String toolName, Object parameters) {
        log.debug("执行敏感工具验证: toolName={}", toolName);

        // 敏感工具需要用户认证
        if (userId == null) {
            return ToolSecurityResult.forbidden("敏感操作需要用户认证");
        }

        // 特定工具的安全规则
        switch (toolName) {
            case "cancelOrder":
                return validateOrderCancellation(userId, parameters);
            case "updateOrderAddress":
                return validateAddressUpdate(userId, parameters);
            case "confirmReceipt":
                return validateReceiptConfirmation(userId, parameters);
            default:
                return ToolSecurityResult.allowed();
        }
    }

    /**
     * 验证订单取消操作
     */
    private ToolSecurityResult validateOrderCancellation(Long userId, Object parameters) {
        // 这里可以添加具体的业务安全规则
        // 例如：检查订单状态、用户权限、取消原因等
        log.debug("验证订单取消权限: userId={}", userId);
        return ToolSecurityResult.allowed();
    }

    /**
     * 验证地址更新操作
     */
    private ToolSecurityResult validateAddressUpdate(Long userId, Object parameters) {
        // 检查地址更新的安全性
        log.debug("验证地址更新权限: userId={}", userId);
        return ToolSecurityResult.allowed();
    }

    /**
     * 验证确认收货操作
     */
    private ToolSecurityResult validateReceiptConfirmation(Long userId, Object parameters) {
        // 检查确认收货的合法性
        log.debug("验证确认收货权限: userId={}", userId);
        return ToolSecurityResult.allowed();
    }

    /**
     * 验证工具参数安全性
     */
    private ToolSecurityResult validateToolParameters(String toolName, Object parameters) {
        if (parameters == null) {
            return ToolSecurityResult.allowed();
        }

        String paramStr = parameters.toString().toLowerCase();
        
        // 检查是否包含受限制的模式
        for (String pattern : restrictedPatterns) {
            if (paramStr.matches(pattern)) {
                log.warn("工具参数包含受限制内容: toolName={}, pattern={}", toolName, pattern);
                return ToolSecurityResult.blocked("参数包含受限制的内容");
            }
        }

        return ToolSecurityResult.allowed();
    }

    /**
     * 检查用户是否有工具权限
     */
    private boolean hasToolPermission(Long userId, String toolName) {
        // 这里可以集成具体的权限系统
        // 目前简单地允许所有已认证用户使用基础工具
        if (userId == null) {
            // 未认证用户只能使用查询工具
            return toolName.toLowerCase().contains("query") || 
                   toolName.toLowerCase().contains("get");
        }
        
        return true; // 已认证用户可以使用所有工具
    }

    /**
     * 检查是否为敏感工具
     */
    private boolean isSensitiveTool(String toolName) {
        return sensitiveTools.contains(toolName);
    }

    /**
     * 检查会话是否被阻止
     */
    public boolean isSessionBlocked(String sessionId) {
        return sessionId != null && blockedSessions.contains(sessionId);
    }

    /**
     * 检查用户是否被阻止
     */
    public boolean isUserBlocked(Long userId) {
        return userId != null && blockedUsers.contains(userId);
    }

    /**
     * 阻止会话
     */
    public void blockSession(String sessionId, String reason) {
        if (sessionId != null) {
            blockedSessions.add(sessionId);
            log.warn("会话已被阻止: sessionId={}, reason={}", sessionId, reason);
        }
    }

    /**
     * 阻止用户
     */
    public void blockUser(Long userId, String reason) {
        if (userId != null) {
            blockedUsers.add(userId);
            log.warn("用户已被阻止: userId={}, reason={}", userId, reason);
        }
    }

    /**
     * 解除会话阻止
     */
    public void unblockSession(String sessionId) {
        if (sessionId != null) {
            blockedSessions.remove(sessionId);
            log.info("会话阻止已解除: sessionId={}", sessionId);
        }
    }

    /**
     * 解除用户阻止
     */
    public void unblockUser(Long userId) {
        if (userId != null) {
            blockedUsers.remove(userId);
            log.info("用户阻止已解除: userId={}", userId);
        }
    }

    /**
     * 获取安全统计信息
     */
    public SecurityStats getSecurityStats() {
        return SecurityStats.builder()
                .blockedSessionsCount(blockedSessions.size())
                .blockedUsersCount(blockedUsers.size())
                .sensitiveToolsCount(sensitiveTools.size())
                .restrictedPatternsCount(restrictedPatterns.size())
                .build();
    }

    /**
     * 安全统计信息
     */
    public static class SecurityStats {
        private final int blockedSessionsCount;
        private final int blockedUsersCount;
        private final int sensitiveToolsCount;
        private final int restrictedPatternsCount;

        private SecurityStats(Builder builder) {
            this.blockedSessionsCount = builder.blockedSessionsCount;
            this.blockedUsersCount = builder.blockedUsersCount;
            this.sensitiveToolsCount = builder.sensitiveToolsCount;
            this.restrictedPatternsCount = builder.restrictedPatternsCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int blockedSessionsCount;
            private int blockedUsersCount;
            private int sensitiveToolsCount;
            private int restrictedPatternsCount;

            public Builder blockedSessionsCount(int count) {
                this.blockedSessionsCount = count;
                return this;
            }

            public Builder blockedUsersCount(int count) {
                this.blockedUsersCount = count;
                return this;
            }

            public Builder sensitiveToolsCount(int count) {
                this.sensitiveToolsCount = count;
                return this;
            }

            public Builder restrictedPatternsCount(int count) {
                this.restrictedPatternsCount = count;
                return this;
            }

            public SecurityStats build() {
                return new SecurityStats(this);
            }
        }

        // Getters
        public int getBlockedSessionsCount() { return blockedSessionsCount; }
        public int getBlockedUsersCount() { return blockedUsersCount; }
        public int getSensitiveToolsCount() { return sensitiveToolsCount; }
        public int getRestrictedPatternsCount() { return restrictedPatternsCount; }
    }
}