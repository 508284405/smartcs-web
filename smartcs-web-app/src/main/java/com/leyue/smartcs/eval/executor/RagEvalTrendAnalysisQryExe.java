package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalTrendAnalysisQry;
import com.leyue.smartcs.dto.eval.RagEvalTrendAnalysisDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalAnalysisGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估趋势分析查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalTrendAnalysisQryExe {
    
    private final RagEvalAnalysisGateway ragEvalAnalysisGateway;
    
    /**
     * 执行趋势分析查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalTrendAnalysisDTO> execute(RagEvalTrendAnalysisQry qry) {
        log.info("执行趋势分析查询");
        
        try {
            // 业务验证
            if (qry.getStartTime() == null || qry.getEndTime() == null) {
                throw new BizException("TIME_RANGE_EMPTY", "开始时间和结束时间不能为空");
            }
            
            if (qry.getTimeGranularity() == null || qry.getTimeGranularity().trim().isEmpty()) {
                throw new BizException("TIME_GRANULARITY_EMPTY", "时间粒度不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalTrendAnalysisDTO result = ragEvalAnalysisGateway.getTrendAnalysis(qry);
            
            log.info("趋势分析查询成功，时间范围: {}, 趋势方向: {}", result.getTimeRange(), result.getTrendDirection());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("趋势分析查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("趋势分析查询失败", e);
            throw new BizException("GET_TREND_ANALYSIS_FAILED", "查询趋势分析失败: " + e.getMessage());
        }
    }
}
