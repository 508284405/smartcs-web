package com.leyue.smartcs.domain.eval.enums;

/**
 * 评估运行状态枚举
 * 
 * @author Claude
 * @since 1.0.0
 */
public enum EvaluationRunStatus {
    
    /**
     * 等待中
     */
    PENDING("pending", "等待中"),
    
    /**
     * 运行中
     */
    RUNNING("running", "运行中"),
    
    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),
    
    /**
     * 失败
     */
    FAILED("failed", "失败"),
    
    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消");
    
    private final String code;
    private final String description;
    
    EvaluationRunStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     */
    public static EvaluationRunStatus fromCode(String code) {
        for (EvaluationRunStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown evaluation run status code: " + code);
    }
}