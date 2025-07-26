package com.leyue.smartcs.model.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型任务数据对象，对应t_model_task表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_model_task")
public class ModelTaskDO extends BaseDO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 任务类型（INFERENCE/TRAINING/EVALUATION/FINE_TUNING）
     */
    private String taskType;

    /**
     * 任务状态（PENDING/RUNNING/COMPLETED/FAILED/CANCELLED）
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
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 开始时间（毫秒时间戳）
     */
    private Long startedAt;

    /**
     * 完成时间（毫秒时间戳）
     */
    private Long completedAt;
}