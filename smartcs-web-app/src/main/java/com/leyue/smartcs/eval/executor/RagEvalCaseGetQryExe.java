package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseGetQry;
import com.leyue.smartcs.dto.eval.RagEvalCaseDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseGetQryExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public SingleResponse<RagEvalCaseDTO> execute(RagEvalCaseGetQry qry) {
        log.info("执行测试用例查询: {}", qry.getCaseId());
        
        try {
            // 业务验证
            if (qry.getCaseId() == null || qry.getCaseId().trim().isEmpty()) {
                throw new BizException("CASE_ID_EMPTY", "测试用例ID不能为空");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            RagEvalCaseDTO testCase = ragEvalCaseGateway.getCase(qry.getCaseId());
            
            if (testCase == null) {
                throw new BizException("CASE_NOT_FOUND", "测试用例不存在: " + qry.getCaseId());
            }
            
            log.debug("测试用例查询成功: {}", testCase.getCaseId());
            return SingleResponse.of(testCase);
            
        } catch (BizException e) {
            log.warn("测试用例查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例查询失败", e);
            throw new BizException("GET_CASE_FAILED", "查询测试用例失败: " + e.getMessage());
        }
    }
}
