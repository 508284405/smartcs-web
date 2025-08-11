package com.leyue.smartcs.domain.eval.event;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 评估运行开始事件
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationRunStartedEvent {
    
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
     * 总用例数
     */
    private Integer totalCases;
    
    /**
     * 开始时间
     */
    private Long startTime;
    
    /**
     * 事件时间戳
     */
    private Long eventTimestamp;
}