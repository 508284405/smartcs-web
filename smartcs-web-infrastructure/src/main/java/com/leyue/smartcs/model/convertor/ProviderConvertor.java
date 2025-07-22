package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import org.mapstruct.Mapper;

/**
 * 模型提供商转换器
 */
@Mapper(componentModel = "spring")
public interface ProviderConvertor {
    
    /**
     * DO转领域对象
     */
    Provider toDomain(ProviderDO providerDO);
    
    /**
     * 领域对象转DO
     */
    ProviderDO toDO(Provider provider);
}