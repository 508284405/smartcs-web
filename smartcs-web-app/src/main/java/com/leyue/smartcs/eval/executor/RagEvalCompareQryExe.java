package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCompareQry;
import com.leyue.smartcs.dto.eval.RagEvalCompareResultDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalAnalysisGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估运行比较查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCompareQryExe {
    
    private final RagEvalAnalysisGateway ragEvalAnalysisGateway;
    
    /**
     * 执行评估运行比较查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalCompareResultDTO> execute(RagEvalCompareQry qry) {
        log.info("执行评估运行比较查询，数量: {}", qry.getRunIds().size());
        
        try {
            // 业务验证
            if (qry.getRunIds() == null || qry.getRunIds().isEmpty()) {
                throw new BizException("RUN_IDS_EMPTY", "运行ID列表不能为空");
            }
            
            if (qry.getRunIds().size() < 2) {
                throw new BizException("INSUFFICIENT_RUNS", "至少需要2个运行ID进行比较");
            }
            
            if (qry.getRunIds().size() > 10) {
                throw new BizException("TOO_MANY_RUNS", "最多只能比较10个运行");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalCompareResultDTO result = ragEvalAnalysisGateway.compareRuns(qry);
            
            log.info("评估运行比较查询成功，比较运行数: {}", result.getRunIds().size());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("评估运行比较查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估运行比较查询失败", e);
            throw new BizException("COMPARE_RUNS_FAILED", "比较运行失败: " + e.getMessage());
        }
    }
}
