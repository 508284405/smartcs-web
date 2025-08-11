package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseUpdateCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例更新命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseUpdateCmdExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例更新命令
     * 
     * @param cmd 更新命令
     * @return 更新结果
     */
    public SingleResponse<RagEvalCaseDTO> execute(RagEvalCaseUpdateCmd cmd) {
        log.info("执行测试用例更新命令: {}", cmd.getCaseId());
        
        try {
            // 业务验证
            if (cmd.getCaseId() == null || cmd.getCaseId().trim().isEmpty()) {
                throw new BizException("CASE_ID_EMPTY", "测试用例ID不能为空");
            }
            
            if (cmd.getQuestion() == null || cmd.getQuestion().trim().isEmpty()) {
                throw new BizException("QUESTION_EMPTY", "问题不能为空");
            }
            
            if (cmd.getExpectedSummary() == null || cmd.getExpectedSummary().trim().isEmpty()) {
                throw new BizException("EXPECTED_SUMMARY_EMPTY", "期望回答摘要不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalCaseDTO testCase = ragEvalCaseGateway.updateCase(cmd);
            
            log.info("测试用例更新成功: {}", testCase.getCaseId());
            return SingleResponse.of(testCase);
            
        } catch (BizException e) {
            log.warn("测试用例更新业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例更新失败", e);
            throw new BizException("UPDATE_CASE_FAILED", "更新测试用例失败: " + e.getMessage());
        }
    }
}
