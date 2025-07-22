package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.dto.model.ProviderPageQry;
import com.leyue.smartcs.model.convertor.ProviderAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 模型提供商分页查询执行器
 */
@Component
@RequiredArgsConstructor
public class ProviderPageQryExe {
    
    private final ProviderGateway providerGateway;
    private final ProviderAppConvertor providerAppConvertor;
    
    public PageResponse<ProviderDTO> execute(ProviderPageQry qry) {
        // 分页查询
        PageResponse<com.leyue.smartcs.domain.model.Provider> pageResponse = 
            providerGateway.pageQuery(qry.getPageIndex(), qry.getPageSize(), qry.getLabel());
        
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