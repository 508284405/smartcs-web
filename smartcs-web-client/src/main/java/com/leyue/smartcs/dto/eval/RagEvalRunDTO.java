package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RAG评估运行DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunDTO {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 应用ID
     */
    private Long appId;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 运行名称
     */
    private String runName;
    
    /**
     * 运行描述
     */
    private String runDescription;
    
    /**
     * 运行类型：FULL, INCREMENTAL, RERUN
     */
    private String runType;
    
    /**
     * 运行状态：PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
     */
    private String status;
    
    /**
     * 进度百分比
     */
    private Double progress;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 发起人用户ID
     */
    private Long initiatorId;
    
    /**
     * 配置参数
     */
    private Map<String, Object> config;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extraProperties;
}
