package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseBatchImportCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseBatchImportResultDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例批量导入命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseBatchImportCmdExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例批量导入命令
     * 
     * @param cmd 批量导入命令
     * @return 导入结果
     */
    public SingleResponse<RagEvalCaseBatchImportResultDTO> execute(RagEvalCaseBatchImportCmd cmd) {
        log.info("执行测试用例批量导入命令，数据集: {}, 数量: {}", cmd.getDatasetId(), cmd.getCases().size());
        
        try {
            // 业务验证
            if (cmd.getDatasetId() == null || cmd.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            if (cmd.getCases() == null || cmd.getCases().isEmpty()) {
                throw new BizException("CASES_EMPTY", "测试用例列表不能为空");
            }
            
            if (cmd.getCases().size() > 1000) {
                throw new BizException("CASES_TOO_MANY", "单次导入测试用例数量不能超过1000");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalCaseBatchImportResultDTO result = ragEvalCaseGateway.batchImportCases(cmd);
            
            log.info("测试用例批量导入成功，总数: {}, 成功: {}, 失败: {}", 
                    result.getTotalCount(), result.getSuccessCount(), result.getFailedCount());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("测试用例批量导入业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例批量导入失败", e);
            throw new BizException("BATCH_IMPORT_CASES_FAILED", "批量导入测试用例失败: " + e.getMessage());
        }
    }
}
