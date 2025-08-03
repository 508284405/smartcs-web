package com.leyue.smartcs.rag.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MCP资源
 * 表示MCP服务器提供的资源
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpResource {

    /**
     * 资源URI
     */
    private String uri;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 资源类型
     */
    private ResourceType type;

    /**
     * 资源内容
     */
    private String content;

    /**
     * 内容类型（MIME类型）
     */
    private String mimeType;

    /**
     * 资源大小（字节）
     */
    private Long size;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 资源元数据
     */
    private Map<String, Object> metadata;

    /**
     * 资源标签
     */
    private java.util.List<String> tags;

    /**
     * 访问权限
     */
    private String permissions;

    /**
     * 资源版本
     */
    private String version;

    /**
     * 资源状态
     */
    private ResourceStatus status;

    /**
     * 服务器ID
     */
    private String serverId;

    /**
     * 是否可缓存
     */
    private Boolean cacheable;

    /**
     * 缓存TTL（秒）
     */
    private Long cacheTtl;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        FILE,           // 文件
        DATABASE,       // 数据库
        API,            // API接口
        DOCUMENT,       // 文档
        IMAGE,          // 图片
        VIDEO,          // 视频
        AUDIO,          // 音频
        CODE,           // 代码
        CONFIG,         // 配置
        LOG,            // 日志
        CUSTOM          // 自定义
    }

    /**
     * 资源状态枚举
     */
    public enum ResourceStatus {
        AVAILABLE,      // 可用
        UNAVAILABLE,    // 不可用
        LOADING,        // 加载中
        ERROR,          // 错误
        EXPIRED         // 已过期
    }
}