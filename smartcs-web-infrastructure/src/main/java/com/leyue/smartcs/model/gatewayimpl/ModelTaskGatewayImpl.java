package com.leyue.smartcs.model.gatewayimpl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leyue.smartcs.domain.model.ModelTask;
import com.leyue.smartcs.domain.model.gateway.ModelTaskGateway;
import com.leyue.smartcs.model.convertor.ModelTaskConvertor;
import com.leyue.smartcs.model.dataobject.ModelTaskDO;
import com.leyue.smartcs.model.mapper.ModelTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型任务Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ModelTaskGatewayImpl implements ModelTaskGateway {
    
    private final ModelTaskMapper taskMapper;
    private final ModelTaskConvertor taskConvertor;
    
    @Override
    public String createInferenceTask(String taskId, Long modelId, String message, String sessionId,
                                    String systemPrompt, Boolean enableRAG, Long knowledgeId,
                                    String inferenceParams, Boolean saveToContext) {
        // 构建输入数据
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("message", message);
        inputData.put("sessionId", sessionId);
        inputData.put("systemPrompt", systemPrompt);
        inputData.put("enableRAG", enableRAG);
        inputData.put("knowledgeId", knowledgeId);
        inputData.put("inferenceParams", inferenceParams);
        inputData.put("saveToContext", saveToContext);
        
        // 创建任务对象
        ModelTask task = ModelTask.builder()
                .taskId(taskId)
                .modelId(modelId)
                .taskType(ModelTask.TaskType.INFERENCE)
                .status(ModelTask.TaskStatus.PENDING)
                .inputData(JSON.toJSONString(inputData))
                .progress(0)
                .priority(0)
                .isDeleted(0)
                .createdAt(System.currentTimeMillis())
                .build();
        
        save(task);
        return taskId;
    }
    
    @Override
    public ModelTask save(ModelTask task) {
        ModelTaskDO taskDO = taskConvertor.toDO(task);
        
        if (task.getTaskId() == null || findOptionalByTaskId(task.getTaskId()).isEmpty()) {
            // 新增
            taskDO.setCreatedAt(System.currentTimeMillis());
            taskMapper.insert(taskDO);
        } else {
            // 更新
            LambdaUpdateWrapper<ModelTaskDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ModelTaskDO::getTaskId, task.getTaskId());
            taskMapper.update(taskDO, wrapper);
        }
        
        return task;
    }
    
    @Override
    public ModelTask findByTaskId(String taskId) {
        Optional<ModelTask> task = findOptionalByTaskId(taskId);
        return task.orElse(null);
    }
    
    @Override
    public Optional<ModelTask> findOptionalByTaskId(String taskId) {
        ModelTaskDO taskDO = taskMapper.selectByTaskId(taskId);
        if (taskDO == null) {
            return Optional.empty();
        }
        
        ModelTask task = taskConvertor.toDomain(taskDO);
        return Optional.of(task);
    }
    
    @Override
    public List<ModelTask> findByModelId(Long modelId) {
        List<ModelTaskDO> taskDOs = taskMapper.selectByModelId(modelId);
        return taskDOs.stream()
                .map(taskConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ModelTask> findByStatus(ModelTask.TaskStatus status) {
        List<ModelTaskDO> taskDOs = taskMapper.selectByStatus(status.name());
        return taskDOs.stream()
                .map(taskConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ModelTask> findPendingTasks(int limit) {
        List<ModelTaskDO> taskDOs = taskMapper.selectPendingTasksByPriority(limit);
        return taskDOs.stream()
                .map(taskConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateStatus(String taskId, ModelTask.TaskStatus status) {
        Optional<ModelTask> taskOpt = findOptionalByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        
        ModelTask task = taskOpt.get();
        task.setStatus(status);
        
        if (status == ModelTask.TaskStatus.RUNNING) {
            task.setStartedAt(System.currentTimeMillis());
        } else if (status == ModelTask.TaskStatus.COMPLETED || 
                   status == ModelTask.TaskStatus.FAILED || 
                   status == ModelTask.TaskStatus.CANCELLED) {
            task.setCompletedAt(System.currentTimeMillis());
        }
        
        save(task);
        return true;
    }
    
    @Override
    public boolean updateProgress(String taskId, int progress) {
        LambdaUpdateWrapper<ModelTaskDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelTaskDO::getTaskId, taskId)
               .set(ModelTaskDO::getProgress, progress);
        
        int result = taskMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public boolean completeTask(String taskId, String outputData) {
        Optional<ModelTask> taskOpt = findOptionalByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        
        ModelTask task = taskOpt.get();
        task.complete(outputData);
        save(task);
        return true;
    }
    
    @Override
    public boolean failTask(String taskId, String errorMessage) {
        Optional<ModelTask> taskOpt = findOptionalByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        
        ModelTask task = taskOpt.get();
        task.fail(errorMessage);
        save(task);
        return true;
    }
    
    @Override
    public boolean cancelTask(String taskId) {
        Optional<ModelTask> taskOpt = findOptionalByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        
        ModelTask task = taskOpt.get();
        task.cancel();
        save(task);
        return true;
    }
    
    @Override
    public boolean deleteTask(String taskId) {
        LambdaUpdateWrapper<ModelTaskDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelTaskDO::getTaskId, taskId)
               .set(ModelTaskDO::getIsDeleted, 1);
        
        int result = taskMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public int cleanExpiredTasks(long expiredTime) {
        LambdaUpdateWrapper<ModelTaskDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.lt(ModelTaskDO::getCreatedAt, expiredTime)
               .in(ModelTaskDO::getStatus, "COMPLETED", "FAILED", "CANCELLED")
               .set(ModelTaskDO::getIsDeleted, 1);
        
        return taskMapper.update(null, wrapper);
    }
}