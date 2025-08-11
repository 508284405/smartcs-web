package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalRunStartCmd;
import com.leyue.smartcs.dto.eval.RagEvalRunDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalRunGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估运行启动命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalRunStartCmdExe {
    
    private final RagEvalRunGateway ragEvalRunGateway;
    
    /**
     * 执行评估运行启动命令
     * 
     * @param cmd 启动命令
     * @return 启动结果
     */
    public SingleResponse<RagEvalRunDTO> execute(RagEvalRunStartCmd cmd) {
        log.info("执行评估运行启动命令，数据集: {}, 运行类型: {}", cmd.getDatasetId(), cmd.getRunType());
        
        try {
            // 业务验证
            if (cmd.getDatasetId() == null || cmd.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            if (cmd.getRunType() == null || cmd.getRunType().trim().isEmpty()) {
                throw new BizException("RUN_TYPE_EMPTY", "运行类型不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalRunDTO run = ragEvalRunGateway.startEvaluation(cmd);
            
            log.info("评估运行启动成功: {}", run.getRunId());
            return SingleResponse.of(run);
            
        } catch (BizException e) {
            log.warn("评估运行启动业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估运行启动失败", e);
            throw new BizException("START_EVALUATION_FAILED", "启动评估失败: " + e.getMessage());
        }
    }
}