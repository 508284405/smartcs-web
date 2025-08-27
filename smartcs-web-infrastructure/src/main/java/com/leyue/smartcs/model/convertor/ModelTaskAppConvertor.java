package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.ModelTask;
import com.leyue.smartcs.dto.model.ModelTaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 模型任务应用层转换器
 */
@Mapper(componentModel = "spring")
public interface ModelTaskAppConvertor {
    
    /**
     * 领域对象转DTO
     */
    @Mapping(target = "taskType", expression = "java(taskTypeToCode(task.getTaskType()))")
    @Mapping(target = "status", expression = "java(statusToCode(task.getStatus()))")
    ModelTaskDTO toDTO(ModelTask task);
    
    /**
     * DTO转领域对象
     */
    @Mapping(target = "taskType", expression = "java(taskTypeFromCode(dto.getTaskType()))")
    @Mapping(target = "status", expression = "java(statusFromCode(dto.getStatus()))")
    ModelTask toDomain(ModelTaskDTO dto);
    
    /**
     * 任务类型枚举转代码
     */
    default String taskTypeToCode(ModelTask.TaskType taskType) {
        return taskType != null ? taskType.name() : null;
    }
    
    /**
     * 任务类型代码转枚举
     */
    default ModelTask.TaskType taskTypeFromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return ModelTask.TaskType.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 任务状态枚举转代码
     */
    default String statusToCode(ModelTask.TaskStatus status) {
        return status != null ? status.name() : null;
    }
    
    /**
     * 任务状态代码转枚举
     */
    default ModelTask.TaskStatus statusFromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return ModelTask.TaskStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}