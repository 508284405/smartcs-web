package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例创建命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseCreateCmdExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例创建命令
     * 
     * @param cmd 创建命令
     * @return 创建结果
     */
    public SingleResponse<RagEvalCaseDTO> execute(RagEvalCaseCreateCmd cmd) {
        log.info("执行测试用例创建命令，数据集: {}", cmd.getDatasetId());
        
        try {
            // 业务验证
            if (cmd.getDatasetId() == null || cmd.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            if (cmd.getQuestion() == null || cmd.getQuestion().trim().isEmpty()) {
                throw new BizException("QUESTION_EMPTY", "问题不能为空");
            }
            
            if (cmd.getExpectedSummary() == null || cmd.getExpectedSummary().trim().isEmpty()) {
                throw new BizException("EXPECTED_SUMMARY_EMPTY", "期望回答摘要不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalCaseDTO testCase = ragEvalCaseGateway.createCase(cmd);
            
            log.info("测试用例创建成功: {}", testCase.getCaseId());
            return SingleResponse.of(testCase);
            
        } catch (BizException e) {
            log.warn("测试用例创建业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例创建失败", e);
            throw new BizException("CREATE_CASE_FAILED", "创建测试用例失败: " + e.getMessage());
        }
    }
}
