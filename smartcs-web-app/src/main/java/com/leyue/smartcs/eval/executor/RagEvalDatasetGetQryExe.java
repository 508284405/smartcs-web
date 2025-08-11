package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalDatasetGetQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalDatasetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估数据集查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetGetQryExe {
    
    private final RagEvalDatasetGateway ragEvalDatasetGateway;
    
    /**
     * 执行数据集查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalDatasetDTO> execute(RagEvalDatasetGetQry qry) {
        log.info("执行数据集查询: {}", qry.getDatasetId());
        
        try {
            // 业务验证
            if (qry.getDatasetId() == null || qry.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalDatasetDTO dataset = ragEvalDatasetGateway.getDataset(qry.getDatasetId());
            
            if (dataset == null) {
                throw new BizException("DATASET_NOT_FOUND", "数据集不存在: " + qry.getDatasetId());
            }
            
            log.debug("数据集查询成功: {}", dataset.getDatasetId());
            return SingleResponse.of(dataset);
            
        } catch (BizException e) {
            log.warn("数据集查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("数据集查询失败", e);
            throw new BizException("GET_DATASET_FAILED", "查询数据集失败: " + e.getMessage());
        }
    }
}
