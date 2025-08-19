package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图目录获取查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogGetQryExe {
    
    private final IntentCatalogGateway catalogGateway;
    
    /**
     * 执行意图目录获取查询
     */
    public SingleResponse<IntentCatalog> execute(Long catalogId) {
        if (catalogId == null) {
            throw new BizException("目录ID不能为空");
        }
        
        IntentCatalog catalog = catalogGateway.findById(catalogId);
        if (catalog == null) {
            throw new BizException("目录不存在，ID: " + catalogId);
        }
        
        log.debug("查询到意图目录: {}", catalog.getName());
        
        return SingleResponse.of(catalog);
    }
}