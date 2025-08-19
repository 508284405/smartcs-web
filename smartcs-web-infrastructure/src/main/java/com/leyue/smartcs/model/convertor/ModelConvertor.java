package com.leyue.smartcs.model.convertor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.FetchFrom;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.model.dataobject.ModelDO;

/**
 * 模型实例转换器
 */
@Mapper(componentModel = "spring")
public interface ModelConvertor {
    
    /**
     * DO转领域对象
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListFromString(modelDO.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromName(modelDO.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromName(modelDO.getStatus()))")
    Model toDomain(ModelDO modelDO);
    
    /**
     * 领域对象转DO
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListToString(model.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromToName(model.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusToName(model.getStatus()))")
    ModelDO toDO(Model model);
    
    /**
     * 模型类型字符串转枚举列表
     */
    default List<ModelType> modelTypeListFromString(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(typeString.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(code -> {
                try {
                    return ModelType.fromCode(code);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(type -> type != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 模型类型枚举列表转字符串
     */
    default String modelTypeListToString(List<ModelType> modelTypes) {
        if (modelTypes == null || modelTypes.isEmpty()) {
            return "";
        }
        return modelTypes.stream()
            .map(ModelType::getCode)
            .collect(Collectors.joining(","));
    }
    
    /**
     * 来源名称转枚举
     */
    default FetchFrom fetchFromFromName(String name) {
        return name != null ? FetchFrom.fromName(name) : null;
    }
    
    /**
     * 来源枚举转名称
     */
    default String fetchFromToName(FetchFrom fetchFrom) {
        return fetchFrom != null ? fetchFrom.name() : null;
    }
    
    /**
     * 状态名称转枚举
     */
    default ModelStatus statusFromName(String name) {
        return name != null ? ModelStatus.fromName(name) : null;
    }
    
    /**
     * 状态枚举转名称
     */
    default String statusToName(ModelStatus status) {
        return status != null ? status.name() : null;
    }
}