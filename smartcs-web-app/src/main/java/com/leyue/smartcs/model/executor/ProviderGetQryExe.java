package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.model.convertor.ProviderAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 获取模型提供商查询执行器
 */
@Component
@RequiredArgsConstructor
public class ProviderGetQryExe {
    
    private final ProviderGateway providerGateway;
    private final ProviderAppConvertor providerAppConvertor;
    
    public SingleResponse<ProviderDTO> execute(Long id) {
        // 查询提供商
        Optional<Provider> providerOpt = providerGateway.findById(id);
        
        if (!providerOpt.isPresent()) {
            return SingleResponse.buildFailure("PROVIDER_NOT_FOUND", "模型提供商不存在");
        }
        
        // 转换为DTO
        ProviderDTO dto = providerAppConvertor.toDTO(providerOpt.get());
        
        return SingleResponse.of(dto);
    }
}