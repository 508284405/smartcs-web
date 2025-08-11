package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalDatasetStatsQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetStatsDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalStatisticsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估数据集统计查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetStatsQryExe {
    
    private final RagEvalStatisticsGateway ragEvalStatisticsGateway;
    
    /**
     * 执行数据集统计查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalDatasetStatsDTO> execute(RagEvalDatasetStatsQry qry) {
        log.info("执行数据集使用统计查询: {}", qry.getDatasetId());
        
        try {
            // 业务验证
            if (qry.getDatasetId() == null || qry.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalDatasetStatsDTO result = ragEvalStatisticsGateway.getDatasetStats(qry);
            
            if (result == null) {
                throw new BizException("DATASET_STATS_NOT_FOUND", "数据集统计不存在: " + qry.getDatasetId());
            }
            
            log.debug("数据集统计查询成功: {}, 总运行数: {}, 总测试用例数: {}", 
                    result.getDatasetId(), result.getTotalRuns(), result.getTotalCases());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("数据集统计查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("数据集统计查询失败", e);
            throw new BizException("GET_DATASET_STATS_FAILED", "查询数据集统计失败: " + e.getMessage());
        }
    }
}
