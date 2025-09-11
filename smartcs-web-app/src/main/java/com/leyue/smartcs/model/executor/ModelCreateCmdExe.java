package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.domainservice.ModelDomainService;
import com.leyue.smartcs.dto.model.ModelCreateCmd;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.model.convertor.ModelAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 创建模型实例命令执行器
 */
@Component
@RequiredArgsConstructor
public class ModelCreateCmdExe {
    
    private final ModelDomainService modelDomainService;
    private final ModelAppConvertor modelAppConvertor;
    
    public SingleResponse<ModelDTO> execute(ModelCreateCmd cmd) {
        // 转换为领域对象
        Model model = modelAppConvertor.toDomain(cmd);
        
        // 执行创建
        Model createdModel = modelDomainService.createModel(model);
        
        // 转换为DTO
        ModelDTO dto = modelAppConvertor.toDTO(createdModel);
        
        return SingleResponse.of(dto);
    }
}