package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalDatasetCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalDatasetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估数据集创建命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetCreateCmdExe {
    
    private final RagEvalDatasetGateway ragEvalDatasetGateway;
    
    /**
     * 执行数据集创建命令
     * 
     * @param cmd 创建命令
     * @return 创建结果
     */
    public SingleResponse<RagEvalDatasetDTO> execute(RagEvalDatasetCreateCmd cmd) {
        log.info("执行数据集创建命令: {}", cmd.getName());
        
        try {
            // 业务验证
            if (cmd.getName() == null || cmd.getName().trim().isEmpty()) {
                throw new BizException("DATASET_NAME_EMPTY", "数据集名称不能为空");
            }
            
            if (cmd.getDescription() == null || cmd.getDescription().trim().isEmpty()) {
                throw new BizException("DATASET_DESCRIPTION_EMPTY", "数据集描述不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalDatasetDTO dataset = ragEvalDatasetGateway.createDataset(cmd);
            
            log.info("数据集创建成功: {}", dataset.getDatasetId());
            return SingleResponse.of(dataset);
            
        } catch (BizException e) {
            log.warn("数据集创建业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("数据集创建失败", e);
            throw new BizException("CREATE_DATASET_FAILED", "创建数据集失败: " + e.getMessage());
        }
    }
}
