package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.gateway.ModelTaskGateway;
import com.leyue.smartcs.dto.model.ModelTaskDTO;
import com.leyue.smartcs.model.convertor.ModelTaskAppConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 模型任务查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelTaskQryExe {

    private final ModelTaskGateway modelTaskGateway;
    private final ModelTaskAppConvertor modelTaskAppConvertor;

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    public SingleResponse<ModelTaskDTO> execute(String taskId) {
        try {
            // 参数校验
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new BizException("任务ID不能为空");
            }

            // 查询任务
            var task = modelTaskGateway.findByTaskId(taskId);
            if (task == null) {
                return SingleResponse.buildFailure("TASK_NOT_FOUND", "任务不存在");
            }

            // 转换为DTO
            ModelTaskDTO dto = modelTaskAppConvertor.toDTO(task);

            return SingleResponse.of(dto);

        } catch (Exception e) {
            log.error("查询任务状态失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new BizException("查询任务状态失败: " + e.getMessage());
        }
    }
}