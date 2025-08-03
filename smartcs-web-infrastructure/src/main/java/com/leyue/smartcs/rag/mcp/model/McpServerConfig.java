package com.leyue.smartcs.rag.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * MCP服务器配置
 * 定义MCP服务器的连接和认证配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerConfig {

    /**
     * 服务器ID
     */
    private String id;

    /**
     * 服务器名称
     */
    private String name;

    /**
     * 服务器描述
     */
    private String description;

    /**
     * 服务器端点URL
     */
    private String endpoint;

    /**
     * 服务器类型
     */
    private ServerType type;

    /**
     * 认证配置
     */
    private AuthConfig auth;

    /**
     * 支持的能力列表
     */
    private List<String> capabilities;

    /**
     * 连接超时时间
     */
    private Duration connectionTimeout;

    /**
     * 读取超时时间
     */
    private Duration readTimeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 扩展配置
     */
    private Map<String, Object> extensions;

    /**
     * 版本号
     */
    private String version;

    /**
     * 服务器标签
     */
    private List<String> tags;

    /**
     * 健康检查配置
     */
    private HealthCheckConfig healthCheck;

    /**
     * 服务器类型枚举
     */
    public enum ServerType {
        HTTP,       // HTTP服务器
        WEBSOCKET,  // WebSocket服务器
        GRPC,       // gRPC服务器
        STDIO       // 标准输入输出
    }

    /**
     * 认证配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthConfig {
        private AuthType type;
        private String username;
        private String password;
        private String token;
        private String apiKey;
        private Map<String, String> headers;
        private Map<String, Object> customAuth;
    }

    /**
     * 认证类型枚举
     */
    public enum AuthType {
        NONE,       // 无认证
        BASIC,      // 基本认证
        BEARER,     // Bearer Token
        API_KEY,    // API Key
        CUSTOM      // 自定义认证
    }

    /**
     * 健康检查配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthCheckConfig {
        private Boolean enabled;
        private Duration interval;
        private Duration timeout;
        private Integer maxFailures;
        private String healthCheckUrl;
    }
}