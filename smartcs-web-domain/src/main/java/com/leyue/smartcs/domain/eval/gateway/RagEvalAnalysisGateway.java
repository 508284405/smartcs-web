package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.RagEvalCompareQry;
import com.leyue.smartcs.dto.eval.RagEvalCompareResultDTO;
import com.leyue.smartcs.dto.eval.RagEvalAbTestQry;
import com.leyue.smartcs.dto.eval.RagEvalAbTestResultDTO;
import com.leyue.smartcs.dto.eval.RagEvalTrendAnalysisQry;
import com.leyue.smartcs.dto.eval.RagEvalTrendAnalysisDTO;

/**
 * RAG评估分析Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalAnalysisGateway {
    
    /**
     * 比较评估运行
     * 
     * @param qry 比较查询
     * @return 比较结果
     */
    RagEvalCompareResultDTO compareRuns(RagEvalCompareQry qry);
    
    /**
     * 分析A/B测试
     * 
     * @param qry A/B测试查询
     * @return A/B测试结果
     */
    RagEvalAbTestResultDTO analyzeAbTest(RagEvalAbTestQry qry);
    
    /**
     * 获取趋势分析
     * 
     * @param qry 趋势分析查询
     * @return 趋势分析结果
     */
    RagEvalTrendAnalysisDTO getTrendAnalysis(RagEvalTrendAnalysisQry qry);
}
