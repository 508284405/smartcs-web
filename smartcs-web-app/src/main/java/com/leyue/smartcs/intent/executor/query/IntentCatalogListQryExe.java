package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图目录列表查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogListQryExe {
    
    private final IntentCatalogGateway catalogGateway;
    
    /**
     * 执行查询所有激活的意图目录
     */
    public MultiResponse<IntentCatalog> execute() {
        List<IntentCatalog> catalogs = catalogGateway.findAllActive();
        
        log.debug("查询到意图目录数量: {}", catalogs.size());
        
        return MultiResponse.of(catalogs);
    }
    
    /**
     * 根据父目录ID查询子目录
     */
    public MultiResponse<IntentCatalog> executeByParentId(Long parentId) {
        List<IntentCatalog> catalogs = catalogGateway.findByParentId(parentId);
        
        log.debug("查询到子目录数量: {}, 父目录ID: {}", catalogs.size(), parentId);
        
        return MultiResponse.of(catalogs);
    }
}