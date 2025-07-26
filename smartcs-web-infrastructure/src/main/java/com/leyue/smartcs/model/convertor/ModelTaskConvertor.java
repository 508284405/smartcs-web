package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.ModelTask;
import com.leyue.smartcs.model.dataobject.ModelTaskDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 模型任务转换器
 */
@Mapper(componentModel = "spring")
public interface ModelTaskConvertor {
    
    /**
     * DO转领域对象
     */
    @Mapping(target = "taskType", expression = "java(taskTypeFromCode(modelTaskDO.getTaskType()))")
    @Mapping(target = "status", expression = "java(statusFromCode(modelTaskDO.getStatus()))")
    ModelTask toDomain(ModelTaskDO modelTaskDO);
    
    /**
     * 领域对象转DO
     */
    @Mapping(target = "taskType", expression = "java(taskTypeToCode(modelTask.getTaskType()))")
    @Mapping(target = "status", expression = "java(statusToCode(modelTask.getStatus()))")
    ModelTaskDO toDO(ModelTask modelTask);
    
    /**
     * 任务类型代码转枚举
     */
    default ModelTask.TaskType taskTypeFromCode(String code) {
        return code != null ? ModelTask.TaskType.valueOf(code) : null;
    }
    
    /**
     * 任务类型枚举转代码
     */
    default String taskTypeToCode(ModelTask.TaskType taskType) {
        return taskType != null ? taskType.name() : null;
    }
    
    /**
     * 状态代码转枚举
     */
    default ModelTask.TaskStatus statusFromCode(String code) {
        return code != null ? ModelTask.TaskStatus.valueOf(code) : null;
    }
    
    /**
     * 状态枚举转代码
     */
    default String statusToCode(ModelTask.TaskStatus status) {
        return status != null ? status.name() : null;
    }
}