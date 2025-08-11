package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.RagEvalExportCmd;
import com.leyue.smartcs.dto.eval.RagEvalExportResultDTO;

/**
 * RAG评估导出Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalExportGateway {
    
    /**
     * 导出评估结果
     * 
     * @param cmd 导出命令
     * @return 导出结果
     */
    RagEvalExportResultDTO exportResults(RagEvalExportCmd cmd);
}
