package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.RagasServiceStatusDTO;
import com.leyue.smartcs.dto.eval.RagasConnectionTestCmd;
import com.leyue.smartcs.dto.eval.RagasConnectionTestResultDTO;

/**
 * RAGAS服务Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagasServiceGateway {
    
    /**
     * 获取RAGAS服务状态
     * 
     * @return 服务状态
     */
    RagasServiceStatusDTO getServiceStatus();
    
    /**
     * 测试RAGAS服务连接
     * 
     * @param cmd 连接测试命令
     * @return 连接测试结果
     */
    RagasConnectionTestResultDTO testConnection(RagasConnectionTestCmd cmd);
}
