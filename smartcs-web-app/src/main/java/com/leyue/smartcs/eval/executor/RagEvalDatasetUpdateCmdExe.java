package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalDatasetUpdateCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalDatasetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估数据集更新命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetUpdateCmdExe {
    
    private final RagEvalDatasetGateway ragEvalDatasetGateway;
    
    /**
     * 执行数据集更新命令
     * 
     * @param cmd 更新命令
     * @return 更新结果
     */
    public SingleResponse<RagEvalDatasetDTO> execute(RagEvalDatasetUpdateCmd cmd) {
        log.info("执行数据集更新命令: {}", cmd.getDatasetId());
        
        try {
            // 业务验证
            if (cmd.getDatasetId() == null || cmd.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            if (cmd.getName() == null || cmd.getName().trim().isEmpty()) {
                throw new BizException("DATASET_NAME_EMPTY", "数据集名称不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalDatasetDTO dataset = ragEvalDatasetGateway.updateDataset(cmd);
            
            log.info("数据集更新成功: {}", dataset.getDatasetId());
            return SingleResponse.of(dataset);
            
        } catch (BizException e) {
            log.warn("数据集更新业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("数据集更新失败", e);
            throw new BizException("UPDATE_DATASET_FAILED", "更新数据集失败: " + e.getMessage());
        }
    }
}
