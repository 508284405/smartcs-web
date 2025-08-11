package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseDeleteCmd;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例删除命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseDeleteCmdExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例删除命令
     * 
     * @param cmd 删除命令
     * @return 删除结果
     */
    public Response execute(RagEvalCaseDeleteCmd cmd) {
        log.info("执行测试用例删除命令: {}", cmd.getCaseId());
        
        try {
            // 业务验证
            if (cmd.getCaseId() == null || cmd.getCaseId().trim().isEmpty()) {
                throw new BizException("CASE_ID_EMPTY", "测试用例ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            ragEvalCaseGateway.deleteCase(cmd.getCaseId());
            
            log.info("测试用例删除成功: {}", cmd.getCaseId());
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("测试用例删除业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例删除失败", e);
            throw new BizException("DELETE_CASE_FAILED", "删除测试用例失败: " + e.getMessage());
        }
    }
}
