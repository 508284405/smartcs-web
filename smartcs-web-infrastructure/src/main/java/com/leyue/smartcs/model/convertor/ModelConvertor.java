package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.FetchFrom;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.model.dataobject.ModelDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 模型实例转换器
 */
@Mapper(componentModel = "spring")
public interface ModelConvertor {
    
    /**
     * DO转领域对象
     */
    @Mapping(target = "modelType", expression = "java(modelTypeFromCode(modelDO.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromCode(modelDO.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromCode(modelDO.getStatus()))")
    Model toDomain(ModelDO modelDO);
    
    /**
     * 领域对象转DO
     */
    @Mapping(target = "modelType", expression = "java(modelTypeToCode(model.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromToCode(model.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusToCode(model.getStatus()))")
    ModelDO toDO(Model model);
    
    /**
     * 模型类型代码转枚举
     */
    default ModelType modelTypeFromCode(String code) {
        return code != null ? ModelType.fromCode(code) : null;
    }
    
    /**
     * 模型类型枚举转代码
     */
    default String modelTypeToCode(ModelType modelType) {
        return modelType != null ? modelType.getCode() : null;
    }
    
    /**
     * 来源代码转枚举
     */
    default FetchFrom fetchFromFromCode(String code) {
        return code != null ? FetchFrom.fromCode(code) : null;
    }
    
    /**
     * 来源枚举转代码
     */
    default String fetchFromToCode(FetchFrom fetchFrom) {
        return fetchFrom != null ? fetchFrom.getCode() : null;
    }
    
    /**
     * 状态代码转枚举
     */
    default ModelStatus statusFromCode(String code) {
        return code != null ? ModelStatus.fromCode(code) : null;
    }
    
    /**
     * 状态枚举转代码
     */
    default String statusToCode(ModelStatus status) {
        return status != null ? status.getCode() : null;
    }
}