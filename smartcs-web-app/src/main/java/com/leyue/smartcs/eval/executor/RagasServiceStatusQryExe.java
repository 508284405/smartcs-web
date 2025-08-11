package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagasServiceStatusDTO;
import com.leyue.smartcs.domain.eval.gateway.RagasServiceGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAGAS服务状态查询执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagasServiceStatusQryExe {
    
    private final RagasServiceGateway ragasServiceGateway;
    
    /**
     * 执行RAGAS服务状态查询
     * 
     * @return 查询结果
     */
    public SingleResponse<RagasServiceStatusDTO> execute() {
        log.info("执行RAGAS服务状态查询");
        
        try {
            // 通过Gateway接口调用Infrastructure层能力
            RagasServiceStatusDTO status = ragasServiceGateway.getServiceStatus();
            
            if (status == null) {
                throw new BizException("SERVICE_STATUS_NOT_FOUND", "RAGAS服务状态不存在");
            }
            
            log.debug("RAGAS服务状态查询成功，服务名: {}, 状态: {}, 版本: {}", 
                    status.getServiceName(), status.getStatus(), status.getVersion());
            return SingleResponse.of(status);
            
        } catch (BizException e) {
            log.warn("RAGAS服务状态查询业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("RAGAS服务状态查询失败", e);
            throw new BizException("GET_RAGAS_STATUS_FAILED", "查询RAGAS服务状态失败: " + e.getMessage());
        }
    }
}
