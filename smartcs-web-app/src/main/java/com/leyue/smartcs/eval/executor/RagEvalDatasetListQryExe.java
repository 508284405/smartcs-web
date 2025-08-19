package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagEvalDatasetListQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.domain.eval.gateway.RagEvalDatasetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG评估数据集列表查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetListQryExe {
    
    private final RagEvalDatasetGateway ragEvalDatasetGateway;
    
    /**
     * 执行数据集列表查询
     * 
     * @param qry 查询对象
     * @return 查询结果
     */
    public PageResponse<RagEvalDatasetDTO> execute(RagEvalDatasetListQry qry) {
        log.info("执行数据集列表查询，页码: {}, 页大小: {}", qry.getPageNum(), qry.getPageSize());
        
        try {
            // 业务验证
            if (qry.getPageNum() <= 0) {
                throw new BizException("INVALID_PAGE_NUM", "页码必须大于0");
            }
            
            if (qry.getPageSize() <= 0 || qry.getPageSize() > 1000) {
                throw new BizException("INVALID_PAGE_SIZE", "页大小必须在1-1000之间");
            }
            
            // 通过Gateway接口调用Infrastructure层能力
            PageResponse<RagEvalDatasetDTO> response = ragEvalDatasetGateway.listDatasets(qry);
            
            log.debug("数据集列表查询成功，总数: {}", response.getTotalCount());
            return response;
            
        } catch (BizException e) {
            log.warn("数据集列表查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("数据集列表查询失败", e);
            throw new BizException("LIST_DATASETS_FAILED", "查询数据集列表失败: " + e.getMessage());
        }
    }
}
