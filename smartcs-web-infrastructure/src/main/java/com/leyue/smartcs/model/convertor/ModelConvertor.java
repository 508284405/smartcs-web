package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.FetchFrom;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.model.dataobject.ModelDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型实例转换器
 */
@Mapper(componentModel = "spring")
public interface ModelConvertor {
    
    /**
     * DO转领域对象
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListFromString(modelDO.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromCode(modelDO.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromCode(modelDO.getStatus()))")
    Model toDomain(ModelDO modelDO);
    
    /**
     * 领域对象转DO
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListToString(model.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromToCode(model.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusToCode(model.getStatus()))")
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