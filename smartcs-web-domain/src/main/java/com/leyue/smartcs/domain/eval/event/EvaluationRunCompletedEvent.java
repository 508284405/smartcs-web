package com.leyue.smartcs.domain.eval.event;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 评估运行完成事件
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationRunCompletedEvent {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 运行类型
     */
    private String runType;
    
    /**
     * 发起人ID
     */
    private Long initiatorId;
    
    /**
     * 运行状态
     */
    private String status;
    
    /**
     * 总用例数
     */
    private Integer totalCases;
    
    /**
     * 完成用例数
     */
    private Integer completedCases;
    
    /**
     * 失败用例数
     */
    private Integer failedCases;
    
    /**
     * 运行时长（毫秒）
     */
    private Long durationMs;
    
    /**
     * 主要指标摘要
     */
    private Map<String, BigDecimal> keyMetrics;
    
    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    private Long startTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 事件时间戳
     */
    private Long eventTimestamp;
    
    /**
     * 检查是否成功完成
     */
    public boolean isSuccessful() {
        return "completed".equals(status);
    }
    
    /**
     * 计算成功率
     */
    public Double getSuccessRate() {
        if (totalCases == null || totalCases == 0) {
            return null;
        }
        return (double) completedCases / totalCases;
    }
}