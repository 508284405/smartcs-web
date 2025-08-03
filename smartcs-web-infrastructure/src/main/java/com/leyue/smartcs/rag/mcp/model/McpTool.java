package com.leyue.smartcs.rag.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP工具
 * 表示MCP服务器提供的工具
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpTool {

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 工具版本
     */
    private String version;

    /**
     * 工具类型
     */
    private ToolType type;

    /**
     * 输入参数定义
     */
    private Map<String, ParameterDefinition> parameters;

    /**
     * 必需参数列表
     */
    private List<String> requiredParameters;

    /**
     * 输出格式定义
     */
    private OutputDefinition output;

    /**
     * 工具标签
     */
    private List<String> tags;

    /**
     * 执行超时时间（毫秒）
     */
    private Long timeout;

    /**
     * 是否支持异步执行
     */
    private Boolean async;

    /**
     * 工具权限要求
     */
    private List<String> permissions;

    /**
     * 服务器ID
     */
    private String serverId;

    /**
     * 工具状态
     */
    private ToolStatus status;

    /**
     * 使用示例
     */
    private List<ToolExample> examples;

    /**
     * 工具限制
     */
    private ToolLimits limits;

    /**
     * 工具类型枚举
     */
    public enum ToolType {
        FUNCTION,       // 函数工具
        COMMAND,        // 命令工具
        SCRIPT,         // 脚本工具
        API,            // API工具
        QUERY,          // 查询工具
        ANALYSIS,       // 分析工具
        TRANSFORM,      // 转换工具
        CUSTOM          // 自定义工具
    }

    /**
     * 工具状态枚举
     */
    public enum ToolStatus {
        AVAILABLE,      // 可用
        UNAVAILABLE,    // 不可用
        DEPRECATED,     // 已弃用
        EXPERIMENTAL,   // 实验性
        MAINTENANCE     // 维护中
    }

    /**
     * 参数定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDefinition {
        private String name;
        private String type;
        private String description;
        private Object defaultValue;
        private List<Object> allowedValues;
        private String pattern;
        private Boolean required;
        private Map<String, Object> constraints;
    }

    /**
     * 输出定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutputDefinition {
        private String type;
        private String description;
        private String format;
        private Map<String, Object> schema;
    }

    /**
     * 工具示例
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolExample {
        private String name;
        private String description;
        private Map<String, Object> input;
        private Object output;
    }

    /**
     * 工具限制
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolLimits {
        private Integer maxConcurrentExecutions;
        private Long maxExecutionTime;
        private Integer maxRetries;
        private Map<String, Object> resourceLimits;
    }
}