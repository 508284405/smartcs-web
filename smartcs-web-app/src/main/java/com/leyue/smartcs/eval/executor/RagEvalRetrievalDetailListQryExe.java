package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalRetrievalDetailListQry;
import com.leyue.smartcs.dto.eval.RagEvalRetrievalDetailDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalMetricsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG检索详情列表查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalRetrievalDetailListQryExe {
    
    private final RagEvalMetricsGateway ragEvalMetricsGateway;
    
    /**
     * 执行检索详情列表查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public PageResponse<RagEvalRetrievalDetailDTO> execute(RagEvalRetrievalDetailListQry qry) {
        log.info("执行检索详情列表查询: {}", qry.getRunId());
        
        try {
            // 业务验证
            if (qry.getRunId() == null || qry.getRunId().trim().isEmpty()) {
                throw new BizException("RUN_ID_EMPTY", "运行ID不能为空");
            }
            
            if (qry.getPageNum() <= 0) {
                throw new BizException("INVALID_PAGE_NUM", "页码必须大于0");
            }
            
            if (qry.getPageSize() <= 0 || qry.getPageSize() > 1000) {
                throw new BizException("INVALID_PAGE_SIZE", "页大小必须在1-1000之间");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            PageResponse<RagEvalRetrievalDetailDTO> response = ragEvalMetricsGateway.listRetrievalDetails(qry);
            
            log.debug("检索详情列表查询成功，总数: {}", response.getTotalCount());
            return response;
            
        } catch (BizException e) {
            log.warn("检索详情列表查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("检索详情列表查询失败", e);
            throw new BizException("LIST_RETRIEVAL_DETAILS_FAILED", "查询检索详情列表失败: " + e.getMessage());
        }
    }
}
