package com.leyue.smartcs.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.model.dataobject.ModelTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型任务Mapper接口
 */
@Mapper
public interface ModelTaskMapper extends BaseMapper<ModelTaskDO> {
    
    /**
     * 根据任务ID查询（不包含已删除的）
     * @param taskId 任务ID
     * @return 任务信息
     */
    ModelTaskDO selectByTaskId(@Param("taskId") String taskId);
    
    /**
     * 检查任务ID是否已存在（不包含已删除的）
     * @param taskId 任务ID
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    int countByTaskId(@Param("taskId") String taskId, @Param("excludeId") Long excludeId);
    
    /**
     * 根据模型ID查询任务列表（不包含已删除的）
     * @param modelId 模型ID
     * @return 任务列表
     */
    List<ModelTaskDO> selectByModelId(@Param("modelId") Long modelId);
    
    /**
     * 根据任务类型查询任务
     * @param taskType 任务类型
     * @return 任务列表
     */
    List<ModelTaskDO> selectByTaskType(@Param("taskType") String taskType);
    
    /**
     * 根据状态查询任务
     * @param status 状态
     * @return 任务列表
     */
    List<ModelTaskDO> selectByStatus(@Param("status") String status);
    
    /**
     * 查询运行中的任务
     * @return 运行中的任务列表
     */
    List<ModelTaskDO> selectRunningTasks();
    
    /**
     * 根据优先级查询待执行任务
     * @param limit 限制数量
     * @return 待执行任务列表
     */
    List<ModelTaskDO> selectPendingTasksByPriority(@Param("limit") int limit);
    
    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 状态
     * @return 更新条数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 更新任务进度
     * @param id 任务ID
     * @param progress 进度
     * @return 更新条数
     */
    int updateProgress(@Param("id") Long id, @Param("progress") Integer progress);
}