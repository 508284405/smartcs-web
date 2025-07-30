package com.leyue.smartcs.app.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.app.convertor.AiAppAppConvertor;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.dto.app.AiAppDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI应用查询执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppGetQryExe {

    private final AiAppGateway aiAppGateway;
    
    /**
     * 根据ID查询AI应用
     */
    public SingleResponse<AiAppDTO> execute(Long id) {
        log.info("查询AI应用, ID: {}", id);
        
        try {
            if (id == null) {
                throw new BizException("INVALID_PARAM", "应用ID不能为空");
            }
            
            AiApp aiApp = aiAppGateway.getById(id);
            if (aiApp == null) {
                throw new BizException("APP_NOT_FOUND", "应用不存在");
            }
            
            AiAppDTO result = AiAppAppConvertor.INSTANCE.domainToDto(aiApp);
            
            log.info("AI应用查询成功, ID: {}, 名称: {}", id, aiApp.getName());
            
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("AI应用查询失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI应用查询失败", e);
            throw new BizException("APP_QUERY_ERROR", "AI应用查询失败: " + e.getMessage());
        }
    }
}