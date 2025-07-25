package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 模型任务DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelTaskDTO extends DTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 任务类型（INFERENCE, TRAINING, EVALUATION等）
     */
    private String taskType;

    /**
     * 任务状态（PENDING, RUNNING, COMPLETED, FAILED等）
     */
    private String status;

    /**
     * 输入数据（JSON格式）
     */
    private String inputData;

    /**
     * 输出数据（JSON格式）
     */
    private String outputData;

    /**
     * 进度百分比（0-100）
     */
    private Integer progress;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 开始时间（毫秒时间戳）
     */
    private Long startedAt;

    /**
     * 完成时间（毫秒时间戳）
     */
    private Long completedAt;

    /**
     * 创建人
     */
    private String createdBy;
}