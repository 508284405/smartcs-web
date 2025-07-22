package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.dto.model.ModelPageQry;
import com.leyue.smartcs.model.convertor.ModelAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 模型实例分页查询执行器
 */
@Component
@RequiredArgsConstructor
public class ModelPageQryExe {
    
    private final ModelGateway modelGateway;
    private final ModelAppConvertor modelAppConvertor;
    
    public PageResponse<ModelDTO> execute(ModelPageQry qry) {
        // 转换枚举参数
        ModelType modelType = qry.getModelType() != null ? ModelType.fromCode(qry.getModelType()) : null;
        ModelStatus status = qry.getStatus() != null ? ModelStatus.fromCode(qry.getStatus()) : null;
        
        // 分页查询
        PageResponse<com.leyue.smartcs.domain.model.Model> pageResponse = 
            modelGateway.pageQuery(qry.getPageIndex(), qry.getPageSize(), 
                qry.getProviderId(), modelType, status);
        
        // 转换为DTO
        PageResponse<ModelDTO> result = PageResponse.of(
            pageResponse.getData().stream()
                .map(modelAppConvertor::toDTO)
                .toList(),
            pageResponse.getTotalCount(),
            pageResponse.getPageSize(),
            pageResponse.getPageIndex()
        );
        
        return result;
    }
}