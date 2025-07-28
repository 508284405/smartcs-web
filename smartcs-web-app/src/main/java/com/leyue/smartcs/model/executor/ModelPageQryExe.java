package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.dto.model.ModelPageQry;
import com.leyue.smartcs.model.convertor.ModelAppConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模型实例分页查询执行器
 */
@Component
@RequiredArgsConstructor
public class ModelPageQryExe {
    
    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;
    private final ModelAppConvertor modelAppConvertor;
    
    public PageResponse<ModelDTO> execute(ModelPageQry qry) {
        // 转换枚举参数
        List<ModelType> modelTypes = null;
        if (qry.getModelType() != null && !qry.getModelType().isEmpty()) {
            modelTypes = qry.getModelType().stream()
                .filter(type -> type != null && !type.trim().isEmpty())
                .map(type -> {
                    try {
                        return ModelType.fromName(type.trim());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            if (modelTypes.isEmpty()) {
                modelTypes = null;
            }
        }
        
        ModelStatus status = qry.getStatus() != null ? ModelStatus.fromCode(qry.getStatus()) : null;
        
        // 分页查询
        PageResponse<com.leyue.smartcs.domain.model.Model> pageResponse = 
            modelGateway.pageQuery(qry.getPageIndex(), qry.getPageSize(), 
                qry.getProviderId(), modelTypes, status);
        
        // 批量获取提供商信息
        List<Long> providerIds = pageResponse.getData().stream()
            .map(com.leyue.smartcs.domain.model.Model::getProviderId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        
        Map<Long, ProviderType> providerNameMap = Map.of();
        if (!providerIds.isEmpty()) {
            providerNameMap = providerIds.stream()
                .map(providerGateway::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Provider::getId, Provider::getProviderType));
        }
        
        // 转换为DTO，包含提供商名称
        final Map<Long, ProviderType> finalProviderNameMap = providerNameMap;
        PageResponse<ModelDTO> result = PageResponse.of(
            pageResponse.getData().stream()
                .map(model -> {
                    ModelDTO dto = modelAppConvertor.toDTO(model);
                    if (model.getProviderId() != null) {
                        dto.setProviderName(finalProviderNameMap.get(model.getProviderId()).name());
                    }
                    return dto;
                })
                .toList(),
            pageResponse.getTotalCount(),
            pageResponse.getPageSize(),
            pageResponse.getPageIndex()
        );
        
        return result;
    }
}