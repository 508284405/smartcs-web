package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalRunDeleteCmd;
import com.leyue.smartcs.domain.eval.gateway.RagEvalRunGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估运行删除命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalRunDeleteCmdExe {
    
    private final RagEvalRunGateway ragEvalRunGateway;
    
    /**
     * 执行评估运行删除命令
     * 
     * @param cmd 删除命令
     * @return 删除结果
     */
    public Response execute(RagEvalRunDeleteCmd cmd) {
        log.info("执行评估运行删除命令: {}", cmd.getRunId());
        
        try {
            // 业务验证
            if (cmd.getRunId() == null || cmd.getRunId().trim().isEmpty()) {
                throw new BizException("RUN_ID_EMPTY", "运行ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            ragEvalRunGateway.deleteRun(cmd.getRunId());
            
            log.info("评估运行删除成功: {}", cmd.getRunId());
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("评估运行删除业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估运行删除失败", e);
            throw new BizException("DELETE_RUN_FAILED", "删除运行失败: " + e.getMessage());
        }
    }
}
