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
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromName(cmd.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromName(cmd.getStatus()))")
    @Mapping(target = "featuresList", ignore = true)
    @Mapping(target = "modelTypeStrings", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Model toDomain(ModelCreateCmd cmd);
    
    /**
     * UpdateCmd转领域对象
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListFromStringList(cmd.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromFromName(cmd.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusFromName(cmd.getStatus()))")
    @Mapping(target = "featuresList", ignore = true)
    @Mapping(target = "modelTypeStrings", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Model toDomain(ModelUpdateCmd cmd);
    
    /**
     * 领域对象转DTO
     */
    @Mapping(target = "modelType", expression = "java(modelTypeListToStringList(model.getModelType()))")
    @Mapping(target = "fetchFrom", expression = "java(fetchFromToName(model.getFetchFrom()))")
    @Mapping(target = "status", expression = "java(statusToName(model.getStatus()))")
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