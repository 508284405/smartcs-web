package com.leyue.smartcs.mcp.ratelimit;

import lombok.Getter;
import lombok.ToString;

/**
 * 速率限制检查结果
 * 
 * <p>封装工具调用速率限制检查的结果，包括是否被限制、限制原因、重置时间等信息。</p>
 * 
 * @author Claude
 */
@Getter
@ToString
public class RateLimitResult {
    
    /**
     * 速率限制状态
     */
    public enum Status {
        ALLOWED,    // 允许调用
        LIMITED,    // 速率受限
        ERROR       // 检查异常
    }
    
    private final Status status;
    private final String message;
    private final long resetTimeMillis; // 限制重置时间（毫秒时间戳）
    private final long timestamp;
    
    private RateLimitResult(Status status, String message, long resetTimeMillis) {
        this.status = status;
        this.message = message;
        this.resetTimeMillis = resetTimeMillis;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建允许调用的结果
     */
    public static RateLimitResult allowed() {
        return new RateLimitResult(Status.ALLOWED, "请求未被速率限制", 0);
    }
    
    /**
     * 创建被限制的结果
     * 
     * @param reason 限制原因
     * @param resetTimeMillis 限制重置时间（毫秒时间戳）
     */
    public static RateLimitResult limited(String reason, long resetTimeMillis) {
        return new RateLimitResult(Status.LIMITED, reason, resetTimeMillis);
    }
    
    /**
     * 创建检查异常的结果
     */
    public static RateLimitResult error(String errorMessage) {
        return new RateLimitResult(Status.ERROR, errorMessage, 0);
    }
    
    /**
     * 检查是否允许调用
     */
    public boolean isAllowed() {
        return status == Status.ALLOWED;
    }
    
    /**
     * 检查是否被速率限制
     */
    public boolean isLimited() {
        return status == Status.LIMITED;
    }
    
    /**
     * 检查是否发生错误
     */
    public boolean isError() {
        return status == Status.ERROR;
    }
    
    /**
     * 获取距离重置时间的秒数
     */
    public long getSecondsUntilReset() {
        if (resetTimeMillis <= 0) {
            return 0;
        }
        return Math.max(0, (resetTimeMillis - System.currentTimeMillis()) / 1000);
    }
    
    /**
     * 检查限制是否已过期
     */
    public boolean isResetTimeExpired() {
        return resetTimeMillis > 0 && System.currentTimeMillis() >= resetTimeMillis;
    }
}