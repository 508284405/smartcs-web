package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalCaseListQry;
import com.leyue.smartcs.dto.eval.RagEvalCaseDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估测试用例列表查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseListQryExe {
    
    private final RagEvalCaseGateway ragEvalCaseGateway;
    
    /**
     * 执行测试用例列表查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public PageResponse<RagEvalCaseDTO> execute(RagEvalCaseListQry qry) {
        log.info("执行测试用例列表查询，数据集: {}", qry.getDatasetId());
        
        try {
            // 业务验证
            if (qry.getDatasetId() == null || qry.getDatasetId().trim().isEmpty()) {
                throw new BizException("DATASET_ID_EMPTY", "数据集ID不能为空");
            }
            
            if (qry.getPageNum() <= 0) {
                throw new BizException("INVALID_PAGE_NUM", "页码必须大于0");
            }
            
            if (qry.getPageSize() <= 0 || qry.getPageSize() > 1000) {
                throw new BizException("INVALID_PAGE_SIZE", "页大小必须在1-1000之间");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            PageResponse<RagEvalCaseDTO> response = ragEvalCaseGateway.listCases(qry);
            
            log.debug("测试用例列表查询成功，总数: {}", response.getTotalCount());
            return response;
            
        } catch (BizException e) {
            log.warn("测试用例列表查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("测试用例列表查询失败", e);
            throw new BizException("LIST_CASES_FAILED", "查询测试用例列表失败: " + e.getMessage());
        }
    }
}
