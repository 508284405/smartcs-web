package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.model.convertor.ModelAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 获取模型实例查询执行器
 */
@Component
@RequiredArgsConstructor
public class ModelGetQryExe {
    
    private final ModelGateway modelGateway;
    private final ModelAppConvertor modelAppConvertor;
    
    public SingleResponse<ModelDTO> execute(Long id) {
        // 查询模型
        Optional<Model> modelOpt = modelGateway.findById(id);
        
        if (!modelOpt.isPresent()) {
            return SingleResponse.buildFailure("MODEL_NOT_FOUND", "模型实例不存在");
        }
        
        // 转换为DTO
        ModelDTO dto = modelAppConvertor.toDTO(modelOpt.get());
        
        return SingleResponse.of(dto);
    }
}