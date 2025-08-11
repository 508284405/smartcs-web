package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDeleteCmd;
import com.leyue.smartcs.domain.eval.gateway.RagEvalDatasetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估数据集删除命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetDeleteCmdExe {
    
    private final RagEvalDatasetGateway ragEvalDatasetGateway;
    
    /**
     * 执行数据集删除命令
     * 
     * @param cmd 删除命令
     * @return 删除结果
     */
    public Response execute(RagEvalDatasetDeleteCmd cmd) {
        log.info("执行数据集删除命令: {}", cmd.getDatasetId());
        
        try {
            // 业务验证
            if (cmd.getDatasetId() == null || cmd.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            ragEvalDatasetGateway.deleteDataset(cmd.getDatasetId());
            
            log.info("数据集删除成功: {}", cmd.getDatasetId());
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("数据集删除业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("数据集删除失败", e);
            throw new BizException("DELETE_DATASET_FAILED", "删除数据集失败: " + e.getMessage());
        }
    }
}
