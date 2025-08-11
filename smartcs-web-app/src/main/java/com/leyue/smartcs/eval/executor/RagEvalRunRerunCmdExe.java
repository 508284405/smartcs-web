package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalRunRerunCmd;
import com.leyue.smartcs.dto.eval.RagEvalRunDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalRunGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估运行重新运行命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalRunRerunCmdExe {
    
    private final RagEvalRunGateway ragEvalRunGateway;
    
    /**
     * 执行评估运行重新运行命令
     * 
     * @param cmd 重新运行命令
     * @return 重新运行结果
     */
    public SingleResponse<RagEvalRunDTO> execute(RagEvalRunRerunCmd cmd) {
        log.info("执行评估运行重新运行命令: {}", cmd.getOriginalRunId());
        
        try {
            // 业务验证
            if (cmd.getOriginalRunId() == null || cmd.getOriginalRunId().trim().isEmpty()) {
                throw new BizException("ORIGINAL_RUN_ID_EMPTY", "原始运行ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalRunDTO run = ragEvalRunGateway.rerunEvaluation(cmd.getOriginalRunId());
            
            log.info("评估运行重新运行成功: {}", run.getRunId());
            return SingleResponse.of(run);
            
        } catch (BizException e) {
            log.warn("评估运行重新运行业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估运行重新运行失败", e);
            throw new BizException("RERUN_EVALUATION_FAILED", "重新运行评估失败: " + e.getMessage());
        }
    }
}
