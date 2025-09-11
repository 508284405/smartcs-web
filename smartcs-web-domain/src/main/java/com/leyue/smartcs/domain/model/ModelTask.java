package com.leyue.smartcs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.alibaba.fastjson2.JSON;
import java.util.Map;

/**
 * 模型任务领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelTask {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 任务类型
     */
    private TaskType taskType;
    
    /**
     * 任务状态
     */
    private TaskStatus status;
    
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
     * 逻辑删除标识
     */
    private Integer isDeleted;
    
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
    
    /**
     * 任务类型枚举
     */
    public enum TaskType {
        INFERENCE("推理"),
        TRAINING("训练"),
        EVALUATION("评估"),
        FINE_TUNING("微调");
        
        private final String description;
        
        TaskType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("待执行"),
        RUNNING("执行中"),
        COMPLETED("已完成"),
        FAILED("执行失败"),
        CANCELLED("已取消");
        
        private final String description;
        
        TaskStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 验证任务配置是否有效
     */
    public boolean isValid() {
        return taskId != null && !taskId.trim().isEmpty()
                && modelId != null
                && taskType != null
                && status != null;
    }
    
    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
    }
    
    /**
     * 是否可以执行
     */
    public boolean canExecute() {
        return status == TaskStatus.PENDING && !isDeleted();
    }
    
    /**
     * 是否正在执行
     */
    public boolean isRunning() {
        return status == TaskStatus.RUNNING;
    }
    
    /**
     * 是否已完成（成功或失败）
     */
    public boolean isFinished() {
        return status == TaskStatus.COMPLETED || status == TaskStatus.FAILED || status == TaskStatus.CANCELLED;
    }
    
    /**
     * 开始执行任务
     */
    public void start() {
        this.status = TaskStatus.RUNNING;
        this.startedAt = System.currentTimeMillis();
        this.progress = 0;
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        this.progress = progress;
    }
    
    /**
     * 完成任务
     */
    public void complete(String outputData) {
        this.status = TaskStatus.COMPLETED;
        this.outputData = outputData;
        this.completedAt = System.currentTimeMillis();
        this.progress = 100;
    }
    
    /**
     * 任务执行失败
     */
    public void fail(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = System.currentTimeMillis();
    }
    
    /**
     * 取消任务
     */
    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.completedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取输入数据映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInputDataMap() {
        if (inputData == null || inputData.trim().isEmpty()) {
            return Map.of();
        }
        try {
            return JSON.parseObject(inputData, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
    
    /**
     * 设置输入数据映射
     */
    public void setInputDataMap(Map<String, Object> inputDataMap) {
        if (inputDataMap == null || inputDataMap.isEmpty()) {
            this.inputData = "{}";
        } else {
            this.inputData = JSON.toJSONString(inputDataMap);
        }
    }
    
    /**
     * 获取输出数据映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOutputDataMap() {
        if (outputData == null || outputData.trim().isEmpty()) {
            return Map.of();
        }
        try {
            return JSON.parseObject(outputData, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
    
    /**
     * 设置输出数据映射
     */
    public void setOutputDataMap(Map<String, Object> outputDataMap) {
        if (outputDataMap == null || outputDataMap.isEmpty()) {
            this.outputData = "{}";
        } else {
            this.outputData = JSON.toJSONString(outputDataMap);
        }
    }
    
    /**
     * 计算执行时长（毫秒）
     */
    public Long getExecutionTime() {
        if (startedAt == null) {
            return null;
        }
        
        Long endTime = completedAt != null ? completedAt : System.currentTimeMillis();
        return endTime - startedAt;
    }
}