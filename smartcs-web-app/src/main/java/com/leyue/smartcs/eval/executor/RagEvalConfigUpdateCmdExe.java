package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalConfigUpdateCmd;
import com.leyue.smartcs.domain.eval.gateway.RagEvalConfigGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估配置更新命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalConfigUpdateCmdExe {
    
    private final RagEvalConfigGateway ragEvalConfigGateway;
    
    /**
     * 执行评估配置更新命令
     * 
     * @param cmd 更新命令
     * @return 更新结果
     */
    public Response execute(RagEvalConfigUpdateCmd cmd) {
        log.info("执行评估配置更新命令");
        
        try {
            // 业务验证
            if (cmd.getDefaultModelId() != null && cmd.getDefaultModelId() <= 0) {
                throw new BizException("INVALID_MODEL_ID", "默认模型ID必须大于0");
            }
            
            if (cmd.getDefaultDatasetId() != null && cmd.getDefaultDatasetId().trim().isEmpty()) {
                throw new BizException("INVALID_DATASET_ID", "默认数据集ID不能为空");
            }
            
            if (cmd.getEvaluationTimeout() != null && (cmd.getEvaluationTimeout() < 60 || cmd.getEvaluationTimeout() > 3600)) {
                throw new BizException("INVALID_TIMEOUT", "评估超时时间必须在60-3600秒之间");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            ragEvalConfigGateway.updateEvalConfig(cmd);
            
            log.info("评估配置更新成功");
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("评估配置更新业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估配置更新失败", e);
            throw new BizException("UPDATE_EVAL_CONFIG_FAILED", "更新评估配置失败: " + e.getMessage());
        }
    }
}
