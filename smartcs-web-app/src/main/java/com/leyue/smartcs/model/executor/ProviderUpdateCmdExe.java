package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.domainservice.ProviderDomainService;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.model.ProviderUpdateCmd;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.model.convertor.ProviderAppConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 更新模型提供商命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderUpdateCmdExe {
    
    private final ProviderDomainService providerDomainService;
    private final ProviderAppConvertor providerAppConvertor;
    private final ModelBeanManagerService modelBeanManagerService;
    private final ProviderGateway providerGateway;
    
    public SingleResponse<ProviderDTO> execute(ProviderUpdateCmd cmd) {
        // 获取更新前的Provider配置，用于比较
        Provider oldProvider = null;
        if (cmd.getId() != null) {
            oldProvider = providerGateway.findById(cmd.getId()).orElse(null);
        }
        
        // 转换为领域对象
        Provider provider = providerAppConvertor.toDomain(cmd);
        
        // 执行更新
        Provider updatedProvider = providerDomainService.updateProvider(provider);
        
        String[] modelTypes = updatedProvider.getSupportedModelTypes().split(",");
        for (String modelType : modelTypes) {
            modelBeanManagerService.restartModelBean(provider, modelType);
        }
        
        // 转换为DTO
        ProviderDTO dto = providerAppConvertor.toDTO(updatedProvider);
        
        return SingleResponse.of(dto);
    }
}