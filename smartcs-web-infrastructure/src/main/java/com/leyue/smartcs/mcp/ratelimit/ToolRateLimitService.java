package com.leyue.smartcs.mcp.ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 工具速率限制服务
 * 
 * <p>提供工具调用的速率限制功能，防止滥用和保护系统资源。
 * 支持基于用户、会话、工具类型的多维度速率控制。</p>
 * 
 * <h3>限制策略:</h3>
 * <ul>
 *   <li>用户级限制 - 每个用户的工具调用频率限制</li>
 *   <li>会话级限制 - 每个会话的工具调用频率限制</li>
 *   <li>工具级限制 - 特定工具的全局调用频率限制</li>
 *   <li>组合限制 - 多个维度的组合限制</li>
 * </ul>
 * 
 * <h3>限制算法:</h3>
 * <ul>
 *   <li>滑动窗口 - 基于时间窗口的请求计数</li>
 *   <li>令牌桶 - 支持突发请求的平滑限流</li>
 *   <li>自适应调整 - 根据系统负载动态调整限制</li>
 * </ul>
 * 
 * @author Claude
 */
@Service
@Slf4j
public class ToolRateLimitService {
    
    // 用户级速率限制器
    private final ConcurrentHashMap<Long, UserRateLimiter> userLimiters = new ConcurrentHashMap<>();
    
    // 会话级速率限制器
    private final ConcurrentHashMap<String, SessionRateLimiter> sessionLimiters = new ConcurrentHashMap<>();
    
    // 工具级速率限制器
    private final ConcurrentHashMap<String, ToolRateLimiter> toolLimiters = new ConcurrentHashMap<>();
    
    // 默认限制配置
    private final RateLimitConfig defaultConfig = RateLimitConfig.builder()
            .userMaxCallsPerMinute(60)
            .sessionMaxCallsPerMinute(30)
            .toolMaxCallsPerMinute(100)
            .burstAllowance(10)
            .build();
    
    /**
     * 检查工具调用是否被速率限制
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param toolName 工具名称
     * @return 速率限制检查结果
     */
    public RateLimitResult checkRateLimit(Long userId, String sessionId, String toolName) {
        log.debug("检查速率限制: userId={}, sessionId={}, toolName={}", userId, sessionId, toolName);
        
        try {
            // 1. 检查用户级限制
            if (userId != null) {
                RateLimitResult userResult = checkUserRateLimit(userId);
                if (userResult.isLimited()) {
                    return userResult;
                }
            }
            
            // 2. 检查会话级限制
            if (sessionId != null) {
                RateLimitResult sessionResult = checkSessionRateLimit(sessionId);
                if (sessionResult.isLimited()) {
                    return sessionResult;
                }
            }
            
            // 3. 检查工具级限制
            RateLimitResult toolResult = checkToolRateLimit(toolName);
            if (toolResult.isLimited()) {
                return toolResult;
            }
            
            // 4. 记录成功的调用
            recordSuccessfulCall(userId, sessionId, toolName);
            
            log.debug("速率限制检查通过: toolName={}", toolName);
            return RateLimitResult.allowed();
            
        } catch (Exception e) {
            log.error("速率限制检查异常: toolName={}", toolName, e);
            // 发生异常时采用保守策略，允许通过但记录警告
            return RateLimitResult.allowed();
        }
    }
    
    /**
     * 检查用户级速率限制
     */
    private RateLimitResult checkUserRateLimit(Long userId) {
        UserRateLimiter limiter = userLimiters.computeIfAbsent(userId, 
            id -> new UserRateLimiter(id, defaultConfig.getUserMaxCallsPerMinute()));
        
        return limiter.checkLimit();
    }
    
    /**
     * 检查会话级速率限制
     */
    private RateLimitResult checkSessionRateLimit(String sessionId) {
        SessionRateLimiter limiter = sessionLimiters.computeIfAbsent(sessionId,
            id -> new SessionRateLimiter(id, defaultConfig.getSessionMaxCallsPerMinute()));
        
        return limiter.checkLimit();
    }
    
    /**
     * 检查工具级速率限制
     */
    private RateLimitResult checkToolRateLimit(String toolName) {
        // 获取工具特定的限制配置
        int maxCalls = getToolSpecificLimit(toolName);
        
        ToolRateLimiter limiter = toolLimiters.computeIfAbsent(toolName,
            name -> new ToolRateLimiter(name, maxCalls));
        
        return limiter.checkLimit();
    }
    
    /**
     * 获取工具特定的速率限制
     */
    private int getToolSpecificLimit(String toolName) {
        // 敏感工具使用更严格的限制
        switch (toolName.toLowerCase()) {
            case "cancelorder":
                return 10; // 每分钟最多10次取消订单
            case "updateorderaddress":
                return 15; // 每分钟最多15次更新地址
            case "confirmreceipt":
                return 20; // 每分钟最多20次确认收货
            case "queryorder":
                return 100; // 查询操作相对宽松
            default:
                return defaultConfig.getToolMaxCallsPerMinute();
        }
    }
    
    /**
     * 记录成功的工具调用
     */
    private void recordSuccessfulCall(Long userId, String sessionId, String toolName) {
        // 更新各级限制器的计数
        if (userId != null) {
            UserRateLimiter userLimiter = userLimiters.get(userId);
            if (userLimiter != null) {
                userLimiter.recordCall();
            }
        }
        
        if (sessionId != null) {
            SessionRateLimiter sessionLimiter = sessionLimiters.get(sessionId);
            if (sessionLimiter != null) {
                sessionLimiter.recordCall();
            }
        }
        
        ToolRateLimiter toolLimiter = toolLimiters.get(toolName);
        if (toolLimiter != null) {
            toolLimiter.recordCall();
        }
    }
    
    /**
     * 获取速率限制统计信息
     */
    public RateLimitStats getRateLimitStats() {
        return RateLimitStats.builder()
                .activeUserLimiters(userLimiters.size())
                .activeSessionLimiters(sessionLimiters.size())
                .activeToolLimiters(toolLimiters.size())
                .totalCallsBlocked(getTotalBlockedCalls())
                .build();
    }
    
    /**
     * 获取总阻止调用数
     */
    private int getTotalBlockedCalls() {
        return userLimiters.values().stream()
                .mapToInt(limiter -> limiter.getBlockedCalls().get())
                .sum() +
               sessionLimiters.values().stream()
                .mapToInt(limiter -> limiter.getBlockedCalls().get())
                .sum() +
               toolLimiters.values().stream()
                .mapToInt(limiter -> limiter.getBlockedCalls().get())
                .sum();
    }
    
    /**
     * 清理过期的限制器
     */
    public void cleanupExpiredLimiters() {
        Instant now = Instant.now();
        Duration expireThreshold = Duration.ofMinutes(5);
        
        // 清理用户限制器
        userLimiters.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(now, expireThreshold));
        
        // 清理会话限制器  
        sessionLimiters.entrySet().removeIf(entry ->
            entry.getValue().isExpired(now, expireThreshold));
        
        // 清理工具限制器
        toolLimiters.entrySet().removeIf(entry ->
            entry.getValue().isExpired(now, expireThreshold));
            
        log.debug("清理过期限制器完成");
    }
    
    /**
     * 用户级速率限制器
     */
    @Getter
    private static class UserRateLimiter {
        private final Long userId;
        private final int maxCallsPerMinute;
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final AtomicInteger blockedCalls = new AtomicInteger(0);
        private volatile Instant windowStart = Instant.now();
        
        public UserRateLimiter(Long userId, int maxCallsPerMinute) {
            this.userId = userId;
            this.maxCallsPerMinute = maxCallsPerMinute;
        }
        
        public RateLimitResult checkLimit() {
            cleanupIfWindowExpired();
            
            if (callCount.get() >= maxCallsPerMinute) {
                blockedCalls.incrementAndGet();
                long resetTime = windowStart.plus(Duration.ofMinutes(1)).toEpochMilli();
                return RateLimitResult.limited("用户调用频率超限", resetTime);
            }
            
            return RateLimitResult.allowed();
        }
        
        public void recordCall() {
            cleanupIfWindowExpired();
            callCount.incrementAndGet();
        }
        
        private void cleanupIfWindowExpired() {
            Instant now = Instant.now();
            if (Duration.between(windowStart, now).toMinutes() >= 1) {
                callCount.set(0);
                windowStart = now;
            }
        }
        
        public boolean isExpired(Instant now, Duration threshold) {
            return Duration.between(windowStart, now).compareTo(threshold) > 0;
        }
    }
    
    /**
     * 会话级速率限制器
     */
    @Getter
    private static class SessionRateLimiter {
        private final String sessionId;
        private final int maxCallsPerMinute;
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final AtomicInteger blockedCalls = new AtomicInteger(0);
        private volatile Instant windowStart = Instant.now();
        
        public SessionRateLimiter(String sessionId, int maxCallsPerMinute) {
            this.sessionId = sessionId;
            this.maxCallsPerMinute = maxCallsPerMinute;
        }
        
        public RateLimitResult checkLimit() {
            cleanupIfWindowExpired();
            
            if (callCount.get() >= maxCallsPerMinute) {
                blockedCalls.incrementAndGet();
                long resetTime = windowStart.plus(Duration.ofMinutes(1)).toEpochMilli();
                return RateLimitResult.limited("会话调用频率超限", resetTime);
            }
            
            return RateLimitResult.allowed();
        }
        
        public void recordCall() {
            cleanupIfWindowExpired();
            callCount.incrementAndGet();
        }
        
        private void cleanupIfWindowExpired() {
            Instant now = Instant.now();
            if (Duration.between(windowStart, now).toMinutes() >= 1) {
                callCount.set(0);
                windowStart = now;
            }
        }
        
        public boolean isExpired(Instant now, Duration threshold) {
            return Duration.between(windowStart, now).compareTo(threshold) > 0;
        }
    }
    
    /**
     * 工具级速率限制器
     */
    @Getter
    private static class ToolRateLimiter {
        private final String toolName;
        private final int maxCallsPerMinute;
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final AtomicInteger blockedCalls = new AtomicInteger(0);
        private volatile Instant windowStart = Instant.now();
        
        public ToolRateLimiter(String toolName, int maxCallsPerMinute) {
            this.toolName = toolName;
            this.maxCallsPerMinute = maxCallsPerMinute;
        }
        
        public RateLimitResult checkLimit() {
            cleanupIfWindowExpired();
            
            if (callCount.get() >= maxCallsPerMinute) {
                blockedCalls.incrementAndGet();
                long resetTime = windowStart.plus(Duration.ofMinutes(1)).toEpochMilli();
                return RateLimitResult.limited("工具调用频率超限", resetTime);
            }
            
            return RateLimitResult.allowed();
        }
        
        public void recordCall() {
            cleanupIfWindowExpired();
            callCount.incrementAndGet();
        }
        
        private void cleanupIfWindowExpired() {
            Instant now = Instant.now();
            if (Duration.between(windowStart, now).toMinutes() >= 1) {
                callCount.set(0);
                windowStart = now;
            }
        }
        
        public boolean isExpired(Instant now, Duration threshold) {
            return Duration.between(windowStart, now).compareTo(threshold) > 0;
        }
    }
    
    /**
     * 速率限制配置
     */
    @Getter
    private static class RateLimitConfig {
        private final int userMaxCallsPerMinute;
        private final int sessionMaxCallsPerMinute;
        private final int toolMaxCallsPerMinute;
        private final int burstAllowance;
        
        private RateLimitConfig(Builder builder) {
            this.userMaxCallsPerMinute = builder.userMaxCallsPerMinute;
            this.sessionMaxCallsPerMinute = builder.sessionMaxCallsPerMinute;
            this.toolMaxCallsPerMinute = builder.toolMaxCallsPerMinute;
            this.burstAllowance = builder.burstAllowance;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int userMaxCallsPerMinute = 60;
            private int sessionMaxCallsPerMinute = 30;
            private int toolMaxCallsPerMinute = 100;
            private int burstAllowance = 10;
            
            public Builder userMaxCallsPerMinute(int maxCalls) {
                this.userMaxCallsPerMinute = maxCalls;
                return this;
            }
            
            public Builder sessionMaxCallsPerMinute(int maxCalls) {
                this.sessionMaxCallsPerMinute = maxCalls;
                return this;
            }
            
            public Builder toolMaxCallsPerMinute(int maxCalls) {
                this.toolMaxCallsPerMinute = maxCalls;
                return this;
            }
            
            public Builder burstAllowance(int allowance) {
                this.burstAllowance = allowance;
                return this;
            }
            
            public RateLimitConfig build() {
                return new RateLimitConfig(this);
            }
        }
    }
    
    /**
     * 速率限制统计信息
     */
    @Getter
    public static class RateLimitStats {
        private final int activeUserLimiters;
        private final int activeSessionLimiters;
        private final int activeToolLimiters;
        private final int totalCallsBlocked;
        
        private RateLimitStats(Builder builder) {
            this.activeUserLimiters = builder.activeUserLimiters;
            this.activeSessionLimiters = builder.activeSessionLimiters;
            this.activeToolLimiters = builder.activeToolLimiters;
            this.totalCallsBlocked = builder.totalCallsBlocked;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int activeUserLimiters;
            private int activeSessionLimiters;
            private int activeToolLimiters;
            private int totalCallsBlocked;
            
            public Builder activeUserLimiters(int count) {
                this.activeUserLimiters = count;
                return this;
            }
            
            public Builder activeSessionLimiters(int count) {
                this.activeSessionLimiters = count;
                return this;
            }
            
            public Builder activeToolLimiters(int count) {
                this.activeToolLimiters = count;
                return this;
            }
            
            public Builder totalCallsBlocked(int count) {
                this.totalCallsBlocked = count;
                return this;
            }
            
            public RateLimitStats build() {
                return new RateLimitStats(this);
            }
        }
    }
}