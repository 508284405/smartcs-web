package com.leyue.smartcs.mcp.security;

import lombok.Getter;
import lombok.ToString;

/**
 * 工具安全验证结果
 * 
 * <p>封装工具调用安全检查的结果，包括是否允许执行、拒绝原因等信息。</p>
 * 
 * @author Claude
 */
@Getter
@ToString
public class ToolSecurityResult {
    
    /**
     * 安全检查状态
     */
    public enum Status {
        ALLOWED,    // 允许执行
        BLOCKED,    // 阻止执行 
        FORBIDDEN,  // 权限不足
        ERROR       // 检查异常
    }
    
    private final Status status;
    private final String message;
    private final long timestamp;
    
    private ToolSecurityResult(Status status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建允许执行的结果
     */
    public static ToolSecurityResult allowed() {
        return new ToolSecurityResult(Status.ALLOWED, "工具调用已通过安全验证");
    }
    
    /**
     * 创建阻止执行的结果
     */
    public static ToolSecurityResult blocked(String reason) {
        return new ToolSecurityResult(Status.BLOCKED, reason);
    }
    
    /**
     * 创建权限不足的结果
     */
    public static ToolSecurityResult forbidden(String reason) {
        return new ToolSecurityResult(Status.FORBIDDEN, reason);
    }
    
    /**
     * 创建检查异常的结果
     */
    public static ToolSecurityResult error(String errorMessage) {
        return new ToolSecurityResult(Status.ERROR, errorMessage);
    }
    
    /**
     * 检查是否允许执行
     */
    public boolean isAllowed() {
        return status == Status.ALLOWED;
    }
    
    /**
     * 检查是否被阻止
     */
    public boolean isBlocked() {
        return status == Status.BLOCKED;
    }
    
    /**
     * 检查是否权限不足
     */
    public boolean isForbidden() {
        return status == Status.FORBIDDEN;
    }
    
    /**
     * 检查是否发生错误
     */
    public boolean isError() {
        return status == Status.ERROR;
    }
}