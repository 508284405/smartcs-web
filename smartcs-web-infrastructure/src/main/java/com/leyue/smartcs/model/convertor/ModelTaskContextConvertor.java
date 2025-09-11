package com.leyue.smartcs.model.convertor;

import org.mapstruct.Mapper;

import com.leyue.smartcs.domain.model.ModelContext;
import com.leyue.smartcs.model.dataobject.ModelTaskContextDO;

/**
 * 模型上下文转换器
 */
@Mapper(componentModel = "spring")
public interface ModelTaskContextConvertor {
    
    /**
     * DO转领域对象
     */
    ModelContext toDomain(ModelTaskContextDO modelTaskContextDO);
    
    /**
     * 领域对象转DO
     */
    ModelTaskContextDO toDO(ModelContext modelContext);
}