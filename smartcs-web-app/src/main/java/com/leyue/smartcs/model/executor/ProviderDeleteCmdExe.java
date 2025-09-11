package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.domainservice.ProviderDomainService;
import com.leyue.smartcs.dto.model.ProviderDeleteCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 删除模型提供商命令执行器
 */
@Component
@RequiredArgsConstructor
public class ProviderDeleteCmdExe {
    
    private final ProviderDomainService providerDomainService;
    
    public SingleResponse<Boolean> execute(ProviderDeleteCmd cmd) {
        // 执行删除
        providerDomainService.deleteProvider(cmd.getId());
        
        return SingleResponse.of(true);
    }
}