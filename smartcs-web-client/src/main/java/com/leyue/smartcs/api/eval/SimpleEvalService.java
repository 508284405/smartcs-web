package com.leyue.smartcs.api.eval;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.eval.*;

/**
 * 简化评估服务接口
 * 提供基于事件驱动架构的RAG评估服务
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface SimpleEvalService {
    
    /**
     * 执行基准集评估
     * 
     * @param request 评估请求
     * @return 评估结果
     */
    SingleResponse<SimpleEvalResponse> runBaselineEvaluation(SimpleEvalRequest request);
    
    /**
     * 获取评估服务状态
     * 
     * @return 服务状态
     */
    SingleResponse<RagasServiceStatusDTO> getServiceStatus();
    
    /**
     * 测试评估服务连接
     * 
     * @param cmd 连接测试命令
     * @return 测试结果
     */
    SingleResponse<RagasConnectionTestResultDTO> testConnection(RagasConnectionTestCmd cmd);
}