package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 意图目录分页查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogPageQryExe {
    
    private final IntentCatalogGateway catalogGateway;
    
    /**
     * 执行意图目录分页查询
     */
    public PageResponse<IntentCatalog> execute(Long parentId, String keyword, int pageNum, int pageSize) {
        
        // Note: Since findByPage method doesn't exist in the gateway, use findByParentId for now
        List<IntentCatalog> allCatalogs = parentId != null ? 
                catalogGateway.findByParentId(parentId) : 
                catalogGateway.findAllActive();
        
        // Filter by keyword if provided
        if (keyword != null && !keyword.trim().isEmpty()) {
            allCatalogs = allCatalogs.stream()
                    .filter(catalog -> catalog.getName().contains(keyword) || 
                            (catalog.getCode() != null && catalog.getCode().contains(keyword)))
                    .collect(Collectors.toList());
        }
        
        // Simple pagination implementation
        int totalCount = allCatalogs.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);
        
        List<IntentCatalog> pagedCatalogs = startIndex < totalCount ? 
                allCatalogs.subList(startIndex, endIndex) : Collections.emptyList();
        
        PageResponse<IntentCatalog> result = new PageResponse<>();
        result.setSuccess(true);
        result.setData(pagedCatalogs);
        result.setTotalCount(totalCount);
        result.setPageSize(pageSize);
        result.setPageIndex(pageNum);
        
        log.debug("查询到意图目录数量: {}, 页码: {}, 每页大小: {}", pagedCatalogs.size(), pageNum, pageSize);
        
        return result;
    }
}