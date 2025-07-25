package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.dto.model.VisualModelProviderQry;
import com.leyue.smartcs.model.convertor.ProviderAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 视觉模型提供商分页查询执行器
 */
@Component
@RequiredArgsConstructor
public class VisualProviderPageQryExe {
    
    private final ProviderGateway providerGateway;
    private final ProviderAppConvertor providerAppConvertor;
    
    public PageResponse<ProviderDTO> execute(VisualModelProviderQry qry) {
        // 分页查询支持视觉识别的提供商
        PageResponse<com.leyue.smartcs.domain.model.Provider> pageResponse = 
            providerGateway.pageVisualProviders(qry.getPageIndex(), qry.getPageSize(), qry.getLabel());
        
        // 转换为DTO
        PageResponse<ProviderDTO> result = PageResponse.of(
            pageResponse.getData().stream()
                .map(providerAppConvertor::toDTO)
                .toList(),
            pageResponse.getTotalCount(),
            pageResponse.getPageSize(),
            pageResponse.getPageIndex()
        );
        
        return result;
    }
}