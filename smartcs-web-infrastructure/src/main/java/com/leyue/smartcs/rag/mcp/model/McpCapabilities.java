package com.leyue.smartcs.rag.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP服务器能力
 * 描述MCP服务器支持的功能和特性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpCapabilities {

    /**
     * 服务器ID
     */
    private String serverId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 协议版本
     */
    private String protocolVersion;

    /**
     * 服务器版本
     */
    private String serverVersion;

    /**
     * 支持的功能列表
     */
    private List<String> capabilities;

    /**
     * 工具支持
     */
    private ToolSupport toolSupport;

    /**
     * 资源支持
     */
    private ResourceSupport resourceSupport;

    /**
     * 提示支持
     */
    private PromptSupport promptSupport;

    /**
     * 采样支持
     */
    private SamplingSupport samplingSupport;

    /**
     * 日志支持
     */
    private LoggingSupport loggingSupport;

    /**
     * 根支持
     */
    private RootsSupport rootsSupport;

    /**
     * 扩展功能
     */
    private Map<String, Object> extensions;

    /**
     * 实验性功能
     */
    private List<String> experimentalFeatures;

    /**
     * 工具支持
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolSupport {
        private Boolean listChanged;
        private Boolean call;
        private List<String> supportedFormats;
        private Integer maxConcurrentCalls;
    }

    /**
     * 资源支持
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceSupport {
        private Boolean subscribe;
        private Boolean listChanged;
        private Boolean read;
        private List<String> supportedSchemes;
        private Long maxResourceSize;
    }

    /**
     * 提示支持
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptSupport {
        private Boolean listChanged;
        private Boolean get;
        private List<String> supportedFormats;
    }

    /**
     * 采样支持
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SamplingSupport {
        private Boolean supported;
        private List<String> supportedModels;
        private Map<String, Object> parameters;
    }

    /**
     * 日志支持
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggingSupport {
        private Boolean supported;
        private List<String> levels;
        private Boolean structured;
    }

    /**
     * 根支持
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RootsSupport {
        private Boolean listChanged;
        private Boolean list;
        private List<String> supportedSchemes;
    }
}