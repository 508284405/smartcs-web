package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.domainservice.ModelDomainService;
import com.leyue.smartcs.dto.model.ModelDeleteCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 删除模型实例命令执行器
 */
@Component
@RequiredArgsConstructor
public class ModelDeleteCmdExe {
    
    private final ModelDomainService modelDomainService;
    
    public SingleResponse<Boolean> execute(ModelDeleteCmd cmd) {
        // 执行删除
        modelDomainService.deleteModel(cmd.getId());
        
        return SingleResponse.of(true);
    }
}