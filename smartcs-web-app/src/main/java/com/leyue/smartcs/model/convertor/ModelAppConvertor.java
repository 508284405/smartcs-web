package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.FetchFrom;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.dto.model.ModelCreateCmd;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.dto.model.ModelUpdateCmd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模型实例应用层转换器
 */
@Mapper(componentModel = "spring")
public interface ModelAppConvertor {
    
    /**
     * CreateCmd转领域对象
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListFromStringList(cmd.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromCode(cmd.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromCode(cmd.getStatus()))")
    Model toDomain(ModelCreateCmd cmd);
    
    /**
     * UpdateCmd转领域对象
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListFromStringList(cmd.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromCode(cmd.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromCode(cmd.getStatus()))")
    Model toDomain(ModelUpdateCmd cmd);
    
    /**
     * 领域对象转DTO
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListToStringList(model.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromToCode(model.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusToCode(model.getStatus()))")
    @Mapping(target = "providerName", ignore = true) // 提供商名称需要单独设置
    ModelDTO toDTO(Model model);
    
    /**
     * 字符串列表转模型类型枚举列表
     */
    default List<ModelType> modelTypeListFromStringList(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        return names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(name -> {
                try {
                    return ModelType.fromName(name.trim());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 模型类型枚举列表转字符串列表
     */
    default List<String> modelTypeListToStringList(List<ModelType> modelTypes) {
        if (modelTypes == null || modelTypes.isEmpty()) {
            return List.of();
        }
        return modelTypes.stream()
            .map(ModelType::name)
            .collect(Collectors.toList());
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