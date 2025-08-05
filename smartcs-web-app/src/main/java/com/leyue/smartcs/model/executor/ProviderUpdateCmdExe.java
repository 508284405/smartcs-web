package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.domainservice.ProviderDomainService;
import com.leyue.smartcs.dto.model.ProviderUpdateCmd;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.model.convertor.ProviderAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 更新模型提供商命令执行器
 */
@Component
@RequiredArgsConstructor
public class ProviderUpdateCmdExe {
    
    private final ProviderDomainService providerDomainService;
    private final ProviderAppConvertor providerAppConvertor;
    
    public SingleResponse<ProviderDTO> execute(ProviderUpdateCmd cmd) {
        // 转换为领域对象
        Provider provider = providerAppConvertor.toDomain(cmd);
        
        // 执行更新
        Provider updatedProvider = providerDomainService.updateProvider(provider);
        
        // 转换为DTO
        ProviderDTO dto = providerAppConvertor.toDTO(updatedProvider);
        
        return SingleResponse.of(dto);
    }
}