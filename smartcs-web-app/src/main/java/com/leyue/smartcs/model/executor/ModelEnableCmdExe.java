package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.domainservice.ModelDomainService;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.dto.model.ModelEnableCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 启用/禁用模型实例命令执行器
 */
@Component
@RequiredArgsConstructor
public class ModelEnableCmdExe {
    
    private final ModelDomainService modelDomainService;
    
    public SingleResponse<Boolean> execute(ModelEnableCmd cmd) {
        // 转换状态枚举
        ModelStatus status = ModelStatus.fromName(cmd.getStatus());
        
        // 执行启用/禁用
        boolean result = modelDomainService.enableModel(cmd.getId(), status);
        
        return SingleResponse.of(result);
    }
}