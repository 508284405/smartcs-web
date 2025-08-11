package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalConfigGetQry;
import com.leyue.smartcs.dto.eval.RagEvalConfigDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalConfigGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估配置查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalConfigGetQryExe {
    
    private final RagEvalConfigGateway ragEvalConfigGateway;
    
    /**
     * 执行评估配置查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalConfigDTO> execute(RagEvalConfigGetQry qry) {
        log.info("执行评估配置查询");
        
        try {
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalConfigDTO config = ragEvalConfigGateway.getEvalConfig(qry);
            
            if (config == null) {
                throw new BizException("CONFIG_NOT_FOUND", "评估配置不存在");
            }
            
            log.debug("评估配置查询成功，默认模型ID: {}, 默认数据集ID: {}", 
                    config.getDefaultModelId(), config.getDefaultDatasetId());
            return SingleResponse.of(config);
            
        } catch (BizException e) {
            log.warn("评估配置查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估配置查询失败", e);
            throw new BizException("GET_EVAL_CONFIG_FAILED", "查询评估配置失败: " + e.getMessage());
        }
    }
}
