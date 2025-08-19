package com.leyue.smartcs.domain.eval.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.eval.RagEvalMetricsDTO;
import com.leyue.smartcs.dto.eval.RagEvalRetrievalDetailDTO;
import com.leyue.smartcs.dto.eval.RagEvalGenerationDetailDTO;
import com.leyue.smartcs.dto.eval.RagEvalRetrievalDetailListQry;
import com.leyue.smartcs.dto.eval.RagEvalGenerationDetailListQry;

/**
 * RAG评估指标Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalMetricsGateway {
    
    /**
     * 根据运行ID查询评估指标汇总
     * 
     * @param runId 运行ID
     * @return 评估指标，如果不存在返回null
     */
    RagEvalMetricsDTO getRunMetrics(String runId);
    
    /**
     * 分页查询检索详情列表
     * 
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<RagEvalRetrievalDetailDTO> listRetrievalDetails(RagEvalRetrievalDetailListQry qry);
    
    /**
     * 分页查询生成详情列表
     * 
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<RagEvalGenerationDetailDTO> listGenerationDetails(RagEvalGenerationDetailListQry qry);
}
