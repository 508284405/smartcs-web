package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalAbTestQry;
import com.leyue.smartcs.dto.eval.RagEvalAbTestResultDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalAnalysisGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估A/B测试分析查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalAbTestQryExe {
    
    private final RagEvalAnalysisGateway ragEvalAnalysisGateway;
    
    /**
     * 执行A/B测试分析查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalAbTestResultDTO> execute(RagEvalAbTestQry qry) {
        log.info("执行A/B测试分析: {} vs {}", qry.getBaselineRunId(), qry.getExperimentRunId());
        
        try {
            // 业务验证
            if (qry.getBaselineRunId() == null || qry.getBaselineRunId().trim().isEmpty()) {
                throw new BizException("BASELINE_RUN_ID_EMPTY", "基准运行ID不能为空");
            }
            
            if (qry.getExperimentRunId() == null || qry.getExperimentRunId().trim().isEmpty()) {
                throw new BizException("EXPERIMENT_RUN_ID_EMPTY", "实验运行ID不能为空");
            }
            
            if (qry.getBaselineRunId().equals(qry.getExperimentRunId())) {
                throw new BizException("SAME_RUN_IDS", "基准运行ID和实验运行ID不能相同");
            }
            
            if (qry.getConfidenceLevel() != null && (qry.getConfidenceLevel() < 0.8 || qry.getConfidenceLevel() > 0.99)) {
                throw new BizException("INVALID_CONFIDENCE_LEVEL", "置信水平必须在0.8-0.99之间");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalAbTestResultDTO result = ragEvalAnalysisGateway.analyzeAbTest(qry);
            
            log.info("A/B测试分析成功，基准运行: {}, 实验运行: {}", result.getBaselineRunId(), result.getExperimentRunId());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("A/B测试分析业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("A/B测试分析失败", e);
            throw new BizException("ANALYZE_AB_TEST_FAILED", "分析A/B测试失败: " + e.getMessage());
        }
    }
}
