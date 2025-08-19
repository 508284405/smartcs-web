package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.RagEvalStatisticsQry;
import com.leyue.smartcs.dto.eval.RagEvalStatisticsDTO;
import com.leyue.smartcs.dto.eval.RagEvalDatasetStatsQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetStatsDTO;
import com.leyue.smartcs.dto.eval.RagEvalModelStatsQry;
import com.leyue.smartcs.dto.eval.RagEvalModelStatsDTO;

/**
 * RAG评估统计Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalStatisticsGateway {
    
    /**
     * 获取统计概览
     * 
     * @param qry 统计查询
     * @return 统计概览
     */
    RagEvalStatisticsDTO getStatistics(RagEvalStatisticsQry qry);
}
