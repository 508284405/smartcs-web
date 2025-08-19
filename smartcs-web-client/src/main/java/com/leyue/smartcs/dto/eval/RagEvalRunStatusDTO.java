package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RAG评估运行状态DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunStatusDTO {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 运行状态
     */
    private String status;
    
    /**
     * 进度百分比
     */
    private Double progress;
    
    /**
     * 当前阶段
     */
    private String currentStage;
    
    /**
     * 阶段描述
     */
    private String stageDescription;
    
    /**
     * 预计剩余时间（秒）
     */
    private Long estimatedRemainingTime;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 警告信息
     */
    private String warningMessage;
    
    /**
     * 状态详情
     */
    private Map<String, Object> statusDetails;
}
