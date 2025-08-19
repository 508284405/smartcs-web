package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 意图目录更新命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogUpdateCmdExe {
    
    private final IntentCatalogGateway catalogGateway;
    
    /**
     * 执行意图目录更新
     */
    public Response execute(Long catalogId, String name, String description, Integer sortOrder) {
        if (catalogId == null) {
            throw new BizException("目录ID不能为空");
        }
        
        IntentCatalog catalog = catalogGateway.findById(catalogId);
        if (catalog == null) {
            throw new BizException("目录不存在，ID: " + catalogId);
        }
        
        if (StringUtils.hasText(name)) {
            catalog.setName(name);
        }
        if (StringUtils.hasText(description)) {
            catalog.setDescription(description);
        }
        if (sortOrder != null) {
            catalog.setSortOrder(sortOrder);
        }
        
        catalog.setUpdatedAt(System.currentTimeMillis());
        catalogGateway.update(catalog);
        
        log.info("意图目录更新成功，ID: {}, 名称: {}", catalog.getId(), catalog.getName());
        
        return Response.buildSuccess();
    }
}