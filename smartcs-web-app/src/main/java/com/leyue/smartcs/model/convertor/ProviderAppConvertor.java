package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import com.leyue.smartcs.dto.model.ProviderCreateCmd;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.dto.model.ProviderUpdateCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 模型提供商应用层转换器
 */
@Mapper(componentModel = "spring")
public interface ProviderAppConvertor {
    
    /**
     * CreateCmd转领域对象
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "supportedModelTypesList", ignore = true)
    Provider toDomain(ProviderCreateCmd cmd);
    
    /**
     * UpdateCmd转领域对象
     */
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "supportedModelTypesList", ignore = true)
    Provider toDomain(ProviderUpdateCmd cmd);
    
    /**
     * 领域对象转DTO
     */
    ProviderDTO toDTO(Provider provider);

    default ProviderType toProviderType(String providerType){
        return ProviderType.valueOf(providerType);
    }
}