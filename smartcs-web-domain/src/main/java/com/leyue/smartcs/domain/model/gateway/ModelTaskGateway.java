package com.leyue.smartcs.domain.model.gateway;

import com.leyue.smartcs.domain.model.ModelTask;

import java.util.List;
import java.util.Optional;

/**
 * 模型任务网关接口
 */
public interface ModelTaskGateway {
    
    /**
     * 创建推理任务
     * 
     * @param taskId 任务ID
     * @param modelId 模型ID
     * @param message 输入消息
     * @param sessionId 会话ID
     * @param systemPrompt 系统Prompt
     * @param knowledgeIds 知识库ID列表
     * @param inferenceParams 推理参数
     * @return 任务ID
     */
    String createInferenceTask(String taskId, Long modelId, String message, String sessionId,
                              String systemPrompt, List<Long> knowledgeIds,
                              String inferenceParams);
    
    /**
     * 保存任务
     * 
     * @param task 任务对象
     * @return 保存后的任务
     */
    ModelTask save(ModelTask task);
    
    /**
     * 根据任务ID查找任务
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    ModelTask findByTaskId(String taskId);
    
    /**
     * 根据任务ID查找任务（可选）
     * 
     * @param taskId 任务ID
     * @return 任务对象（可选）
     */
    Optional<ModelTask> findOptionalByTaskId(String taskId);
    
    /**
     * 根据模型ID查找任务列表
     * 
     * @param modelId 模型ID
     * @return 任务列表
     */
    List<ModelTask> findByModelId(Long modelId);
    
    /**
     * 根据状态查找任务列表
     * 
     * @param status 任务状态
     * @return 任务列表
     */
    List<ModelTask> findByStatus(ModelTask.TaskStatus status);
    
    /**
     * 获取待执行的任务列表
     * 
     * @param limit 限制数量
     * @return 待执行任务列表
     */
    List<ModelTask> findPendingTasks(int limit);
    
    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateTaskStatus(String taskId, ModelTask.TaskStatus status);
    
    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param progress 进度（0-100）
     * @return 是否更新成功
     */
    boolean updateTaskProgress(String taskId, int progress);
    
    /**
     * 更新任务结果
     * 
     * @param taskId 任务ID
     * @param result 任务结果
     * @return 是否更新成功
     */
    boolean updateTaskResult(String taskId, String result);
    
    /**
     * 删除任务
     * 
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(String taskId);
}