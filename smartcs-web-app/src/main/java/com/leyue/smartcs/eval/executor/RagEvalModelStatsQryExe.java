package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalModelStatsQry;
import com.leyue.smartcs.dto.eval.RagEvalModelStatsDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalStatisticsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估模型统计查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalModelStatsQryExe {
    
    private final RagEvalStatisticsGateway ragEvalStatisticsGateway;
    
    /**
     * 执行模型统计查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalModelStatsDTO> execute(RagEvalModelStatsQry qry) {
        log.info("执行模型性能统计查询: {}", qry.getModelId());
        
        try {
            // 业务验证
            if (qry.getModelId() == null) {
                throw new BizException("MODEL_ID_EMPTY", "模型ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalModelStatsDTO result = ragEvalStatisticsGateway.getModelStats(qry);
            
            if (result == null) {
                throw new BizException("MODEL_STATS_NOT_FOUND", "模型统计不存在: " + qry.getModelId());
            }
            
            log.debug("模型统计查询成功: {}, 平均延迟: {}, 成功率: {}", 
                    result.getModelId(), result.getAverageLatency(), result.getSuccessRate());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("模型统计查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("模型统计查询失败", e);
            throw new BizException("GET_MODEL_STATS_FAILED", "查询模型统计失败: " + e.getMessage());
        }
    }
}
