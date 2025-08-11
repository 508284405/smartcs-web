package com.leyue.smartcs.domain.eval.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.eval.RagEvalRunStartCmd;
import com.leyue.smartcs.dto.eval.RagEvalRunDTO;
import com.leyue.smartcs.dto.eval.RagEvalRunDetailDTO;
import com.leyue.smartcs.dto.eval.RagEvalRunListQry;
import com.leyue.smartcs.dto.eval.RagEvalRunStatusDTO;

/**
 * RAG评估运行Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalRunGateway {
    
    /**
     * 启动评估
     * 
     * @param cmd 启动命令
     * @return 运行信息
     */
    RagEvalRunDTO startEvaluation(RagEvalRunStartCmd cmd);
    
    /**
     * 停止评估
     * 
     * @param runId 运行ID
     */
    void stopEvaluation(String runId);
    
    /**
     * 根据ID查询运行详情
     * 
     * @param runId 运行ID
     * @return 运行详情，如果不存在返回null
     */
    RagEvalRunDetailDTO getRunDetail(String runId);
    
    /**
     * 分页查询运行列表
     * 
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<RagEvalRunDTO> listRuns(RagEvalRunListQry qry);
    
    /**
     * 查询运行状态
     * 
     * @param runId 运行ID
     * @return 运行状态，如果不存在返回null
     */
    RagEvalRunStatusDTO getRunStatus(String runId);
    
    /**
     * 重新运行评估
     * 
     * @param originalRunId 原始运行ID
     * @return 新的运行信息
     */
    RagEvalRunDTO rerunEvaluation(String originalRunId);
    
    /**
     * 删除运行
     * 
     * @param runId 运行ID
     */
    void deleteRun(String runId);
}
