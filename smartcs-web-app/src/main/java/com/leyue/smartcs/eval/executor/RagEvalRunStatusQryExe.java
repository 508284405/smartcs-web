package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalRunStatusQry;
import com.leyue.smartcs.dto.eval.RagEvalRunStatusDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalRunGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估运行状态查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalRunStatusQryExe {
    
    private final RagEvalRunGateway ragEvalRunGateway;
    
    /**
     * 执行评估运行状态查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalRunStatusDTO> execute(RagEvalRunStatusQry qry) {
        log.debug("执行评估运行状态查询: {}", qry.getRunId());
        
        try {
            // 业务验证
            if (qry.getRunId() == null || qry.getRunId().trim().isEmpty()) {
                throw new BizException("RUN_ID_EMPTY", "运行ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalRunStatusDTO status = ragEvalRunGateway.getRunStatus(qry.getRunId());
            
            if (status == null) {
                throw new BizException("RUN_NOT_FOUND", "评估运行不存在: " + qry.getRunId());
            }
            
            log.debug("评估运行状态查询成功: {}, 状态: {}", status.getRunId(), status.getStatus());
            return SingleResponse.of(status);
            
        } catch (BizException e) {
            log.warn("评估运行状态查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估运行状态查询失败", e);
            throw new BizException("GET_RUN_STATUS_FAILED", "查询运行状态失败: " + e.getMessage());
        }
    }
}
