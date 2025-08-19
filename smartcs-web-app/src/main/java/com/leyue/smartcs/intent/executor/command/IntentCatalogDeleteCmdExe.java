package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图目录删除命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogDeleteCmdExe {
    
    private final IntentCatalogGateway catalogGateway;
    
    /**
     * 执行意图目录删除（逻辑删除）
     */
    public Response execute(Long catalogId) {
        if (catalogId == null) {
            throw new BizException("目录ID不能为空");
        }
        
        IntentCatalog catalog = catalogGateway.findById(catalogId);
        if (catalog == null) {
            throw new BizException("目录不存在，ID: " + catalogId);
        }
        
        catalog.setIsDeleted(true);
        catalog.setUpdatedAt(System.currentTimeMillis());
        catalogGateway.update(catalog);
        
        log.info("意图目录删除成功，ID: {}, 名称: {}", catalog.getId(), catalog.getName());
        
        return Response.buildSuccess();
    }
}