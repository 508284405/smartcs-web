package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalStatisticsQry;
import com.leyue.smartcs.dto.eval.RagEvalStatisticsDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalStatisticsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估统计概览查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalStatisticsQryExe {
    
    private final RagEvalStatisticsGateway ragEvalStatisticsGateway;
    
    /**
     * 执行统计概览查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalStatisticsDTO> execute(RagEvalStatisticsQry qry) {
        log.info("执行统计概览查询");
        
        try {
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalStatisticsDTO result = ragEvalStatisticsGateway.getStatistics(qry);
            
            log.debug("统计概览查询成功，数据集数: {}, 运行数: {}, 测试用例数: {}", 
                    result.getTotalDatasets(), result.getTotalRuns(), result.getTotalCases());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("统计概览查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("统计概览查询失败", e);
            throw new BizException("GET_STATISTICS_FAILED", "查询统计概览失败: " + e.getMessage());
        }
    }
}
