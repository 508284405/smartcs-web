package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalExportCmd;
import com.leyue.smartcs.dto.eval.RagEvalExportResultDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalExportGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估结果导出命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalExportCmdExe {
    
    private final RagEvalExportGateway ragEvalExportGateway;
    
    /**
     * 执行评估结果导出命令
     * 
     * @param cmd 导出命令
     * @return 导出结果
     */
    public SingleResponse<RagEvalExportResultDTO> execute(RagEvalExportCmd cmd) {
        log.info("执行评估结果导出命令: {}", cmd.getRunId());
        
        try {
            // 业务验证
            if (cmd.getRunId() == null || cmd.getRunId().trim().isEmpty()) {
                throw new BizException("RUN_ID_EMPTY", "运行ID不能为空");
            }
            
            if (cmd.getExportFormat() == null || cmd.getExportFormat().trim().isEmpty()) {
                throw new BizException("EXPORT_FORMAT_EMPTY", "导出格式不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalExportResultDTO result = ragEvalExportGateway.exportResults(cmd);
            
            log.info("评估结果导出成功: {}, 下载链接: {}", result.getRunId(), result.getDownloadUrl());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("评估结果导出业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("评估结果导出失败", e);
            throw new BizException("EXPORT_RESULTS_FAILED", "导出结果失败: " + e.getMessage());
        }
    }
}
