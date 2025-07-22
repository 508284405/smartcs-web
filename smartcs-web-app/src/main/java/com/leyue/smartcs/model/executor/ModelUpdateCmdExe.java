package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.domainservice.ModelDomainService;
import com.leyue.smartcs.dto.model.ModelUpdateCmd;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.model.convertor.ModelAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 更新模型实例命令执行器
 */
@Component
@RequiredArgsConstructor
public class ModelUpdateCmdExe {
    
    private final ModelDomainService modelDomainService;
    private final ModelAppConvertor modelAppConvertor;
    
    public SingleResponse<ModelDTO> execute(ModelUpdateCmd cmd) {
        // 转换为领域对象
        Model model = modelAppConvertor.toDomain(cmd);
        
        // 执行更新
        Model updatedModel = modelDomainService.updateModel(model);
        
        // 转换为DTO
        ModelDTO dto = modelAppConvertor.toDTO(updatedModel);
        
        return SingleResponse.of(dto);
    }
}