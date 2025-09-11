package com.leyue.smartcs.rag.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MCP连接状态
 * 表示MCP服务器的连接状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpConnectionStatus {

    /**
     * 服务器ID
     */
    private String serverId;

    /**
     * 连接状态
     */
    private ConnectionState state;

    /**
     * 连接建立时间
     */
    private LocalDateTime connectedAt;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 断开时间
     */
    private LocalDateTime disconnectedAt;

    /**
     * 连接错误信息
     */
    private String errorMessage;

    /**
     * 重连次数
     */
    private Integer reconnectCount;

    /**
     * 连接延迟（毫秒）
     */
    private Long latency;

    /**
     * 服务器版本
     */
    private String serverVersion;

    /**
     * 协议版本
     */
    private String protocolVersion;

    /**
     * 连接持续时间（秒）
     */
    private Long connectionDuration;

    /**
     * 是否自动重连
     */
    private Boolean autoReconnect;

    /**
     * 健康检查状态
     */
    private HealthStatus healthStatus;

    /**
     * 连接统计
     */
    private ConnectionStats stats;

    /**
     * 连接状态枚举
     */
    public enum ConnectionState {
        CONNECTING,     // 连接中
        CONNECTED,      // 已连接
        DISCONNECTED,   // 已断开
        RECONNECTING,   // 重连中
        FAILED,         // 连接失败
        TIMEOUT         // 连接超时
    }

    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        HEALTHY,        // 健康
        DEGRADED,       // 降级
        UNHEALTHY,      // 不健康
        UNKNOWN         // 未知
    }

    /**
     * 连接统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionStats {
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double averageResponseTime;
        private Long totalBytes;
        private Long uptime;
        private Integer currentConcurrentRequests;
        private Long lastRequestTime;
    }
}