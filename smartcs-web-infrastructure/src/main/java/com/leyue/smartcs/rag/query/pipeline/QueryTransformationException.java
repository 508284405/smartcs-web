package com.leyue.smartcs.rag.query.pipeline;

/**
 * 查询转换异常
 * 当查询转换过程中发生错误时抛出
 * 
 * @author Claude
 */
public class QueryTransformationException extends RuntimeException {
    
    /**
     * 阶段名称
     */
    private final String stageName;
    
    /**
     * 是否可恢复的错误
     */
    private final boolean recoverable;
    
    public QueryTransformationException(String stageName, String message) {
        super(message);
        this.stageName = stageName;
        this.recoverable = true;
    }
    
    public QueryTransformationException(String stageName, String message, boolean recoverable) {
        super(message);
        this.stageName = stageName;
        this.recoverable = recoverable;
    }
    
    public QueryTransformationException(String stageName, String message, Throwable cause) {
        super(message, cause);
        this.stageName = stageName;
        this.recoverable = true;
    }
    
    public QueryTransformationException(String stageName, String message, Throwable cause, boolean recoverable) {
        super(message, cause);
        this.stageName = stageName;
        this.recoverable = recoverable;
    }
    
    /**
     * 获取发生错误的阶段名称
     */
    public String getStageName() {
        return stageName;
    }
    
    /**
     * 检查错误是否可恢复
     */
    public boolean isRecoverable() {
        return recoverable;
    }
}