package com.leyue.smartcs.mcp.audit;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 工具审计记录
 * 
 * <p>记录工具调用的完整信息，包括调用参数、执行结果、性能指标等。
 * 用于审计、监控和安全分析。</p>
 * 
 * @author Claude
 */
@Getter
@Builder
@ToString
public class ToolAuditRecord {
    
    /**
     * 工具调用状态
     */
    public enum Status {
        STARTED,    // 调用开始
        SUCCESS,    // 执行成功
        FAILED,     // 执行失败
        BLOCKED,    // 被阻止
        TIMEOUT     // 执行超时
    }
    
    /**
     * 审计记录ID
     */
    private final String auditId;
    
    /**
     * 会话ID
     */
    private final String sessionId;
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 工具名称
     */
    private final String toolName;
    
    /**
     * 调用参数
     */
    private final String parameters;
    
    /**
     * 执行结果
     */
    private final String result;
    
    /**
     * 错误信息
     */
    private final String errorMessage;
    
    /**
     * 调用状态
     */
    private final Status status;
    
    /**
     * 开始时间
     */
    private final Instant startTime;
    
    /**
     * 结束时间
     */
    private final Instant endTime;
    
    /**
     * 执行时间（毫秒）
     */
    private final Long executionTimeMs;
    
    /**
     * 客户端IP地址
     */
    private final String clientIp;
    
    /**
     * 用户代理
     */
    private final String userAgent;
    
    /**
     * 请求ID（追踪请求链路）
     */
    private final String requestId;
    
    /**
     * 检查是否执行成功
     */
    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }
    
    /**
     * 检查是否执行失败
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }
    
    /**
     * 检查是否被阻止
     */
    public boolean isBlocked() {
        return status == Status.BLOCKED;
    }
    
    /**
     * 检查是否超时
     */
    public boolean isTimeout() {
        return status == Status.TIMEOUT;
    }
    
    /**
     * 获取执行持续时间（秒）
     */
    public double getExecutionDurationSeconds() {
        if (executionTimeMs != null) {
            return executionTimeMs / 1000.0;
        }
        return 0.0;
    }
}