package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.RagEvalConfigGetQry;
import com.leyue.smartcs.dto.eval.RagEvalConfigDTO;
import com.leyue.smartcs.dto.eval.RagEvalConfigUpdateCmd;

/**
 * RAG评估配置Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalConfigGateway {
    
    /**
     * 获取评估配置
     * 
     * @param qry 配置查询
     * @return 评估配置
     */
    RagEvalConfigDTO getEvalConfig(RagEvalConfigGetQry qry);
    
    /**
     * 更新评估配置
     * 
     * @param cmd 配置更新命令
     */
    void updateEvalConfig(RagEvalConfigUpdateCmd cmd);
}
