package com.leyue.smartcs.rag.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MCP调用结果
 * 封装MCP工具调用的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpResult {

    /**
     * 调用是否成功
     */
    private Boolean success;

    /**
     * 结果数据
     */
    private Object data;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 调用ID
     */
    private String callId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 服务器ID
     */
    private String serverId;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 结果类型
     */
    private ResultType resultType;

    /**
     * 结果元数据
     */
    private Map<String, Object> metadata;

    /**
     * 是否来自缓存
     */
    private Boolean fromCache;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 调用参数
     */
    private Map<String, Object> parameters;

    /**
     * 调用日志
     */
    private String log;

    /**
     * 资源使用情况
     */
    private ResourceUsage resourceUsage;

    /**
     * 结果类型枚举
     */
    public enum ResultType {
        SUCCESS,        // 成功
        ERROR,          // 错误
        TIMEOUT,        // 超时
        CANCELLED,      // 已取消
        PARTIAL         // 部分成功
    }

    /**
     * 资源使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceUsage {
        private Long memoryUsage;
        private Long cpuTime;
        private Long networkBytes;
        private Long diskReads;
        private Long diskWrites;
    }

    /**
     * 创建成功结果
     * 
     * @param data 结果数据
     * @return 成功结果
     */
    public static McpResult success(Object data) {
        return McpResult.builder()
                .success(true)
                .data(data)
                .resultType(ResultType.SUCCESS)
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     * 
     * @param errorMessage 错误消息
     * @return 失败结果
     */
    public static McpResult failure(String errorMessage) {
        return McpResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .resultType(ResultType.ERROR)
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建超时结果
     * 
     * @return 超时结果
     */
    public static McpResult timeout() {
        return McpResult.builder()
                .success(false)
                .errorMessage("调用超时")
                .resultType(ResultType.TIMEOUT)
                .endTime(LocalDateTime.now())
                .build();
    }
}