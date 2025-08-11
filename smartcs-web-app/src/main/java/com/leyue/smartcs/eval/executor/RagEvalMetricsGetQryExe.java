package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalMetricsGetQry;
import com.leyue.smartcs.dto.eval.RagEvalMetricsDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalMetricsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估指标查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalMetricsGetQryExe {
    
    private final RagEvalMetricsGateway ragEvalMetricsGateway;
    
    /**
     * 执行评估指标查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalMetricsDTO> execute(RagEvalMetricsGetQry qry) {
        log.info("执行评估指标查询: {}", qry.getRunId());
        
        try {
            // 业务验证
            if (qry.getRunId() == null || qry.getRunId().trim().isEmpty()) {
                throw new BizException("RUN_ID_EMPTY", "运行ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalMetricsDTO metrics = ragEvalMetricsGateway.getRunMetrics(qry.getRunId());
            
            if (metrics == null) {
                throw new BizException("METRICS_NOT_FOUND", "评估指标不存在: " + qry.getRunId());
            }
            
            log.debug("评估指标查询成功: {}", metrics.getRunId());
            return SingleResponse.of(metrics);
            
        } catch (BizException e) {
            log.warn("评估指标查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估指标查询失败", e);
            throw new BizException("GET_METRICS_FAILED", "查询指标失败: " + e.getMessage());
        }
    }
}
