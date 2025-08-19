package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseBatchDeleteCmd;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例批量删除命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseBatchDeleteCmdExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例批量删除命令
     * 
     * @param cmd 批量删除命令
     * @return 删除结果
     */
    public Response execute(RagEvalCaseBatchDeleteCmd cmd) {
        log.info("执行测试用例批量删除命令，数量: {}", cmd.getCaseIds().size());
        
        try {
            // 业务验证
            if (cmd.getCaseIds() == null || cmd.getCaseIds().isEmpty()) {
                throw new BizException("CASE_IDS_EMPTY", "测试用例ID列表不能为空");
            }
            
            if (cmd.getCaseIds().size() > 1000) {
                throw new BizException("TOO_MANY_CASES", "单次批量删除测试用例数量不能超过1000");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            ragEvalCaseGateway.batchDeleteCases(cmd.getCaseIds());
            
            log.info("测试用例批量删除成功，数量: {}", cmd.getCaseIds().size());
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("测试用例批量删除业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例批量删除失败", e);
            throw new BizException("BATCH_DELETE_CASES_FAILED", "批量删除测试用例失败: " + e.getMessage());
        }
    }
}
