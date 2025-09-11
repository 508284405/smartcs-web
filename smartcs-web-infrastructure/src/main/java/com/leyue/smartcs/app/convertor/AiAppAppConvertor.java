package com.leyue.smartcs.app.convertor;

import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;
import com.leyue.smartcs.dto.app.AiAppDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * AI应用应用层转换器
 */
@Mapper
public interface AiAppAppConvertor {
    
    AiAppAppConvertor INSTANCE = Mappers.getMapper(AiAppAppConvertor.class);
    
    /**
     * 领域对象转DTO
     */
    @Mapping(source = "type", target = "type", qualifiedByName = "appTypeToString")
    @Mapping(source = "type", target = "typeName", qualifiedByName = "appTypeToName")
    @Mapping(source = "type", target = "typeDescription", qualifiedByName = "appTypeToDescription")
    @Mapping(source = "status", target = "status", qualifiedByName = "appStatusToString")
    @Mapping(source = "status", target = "statusName", qualifiedByName = "appStatusToName")
    @Mapping(source = ".", target = "usable", qualifiedByName = "domainToUsable")
    @Mapping(source = ".", target = "editable", qualifiedByName = "domainToEditable")
    AiAppDTO domainToDto(AiApp aiApp);
    
    /**
     * 应用类型枚举转字符串
     */
    @Named("appTypeToString")
    default String appTypeToString(AppType type) {
        return type != null ? type.name() : null;
    }
    
    /**
     * 应用类型枚举转名称
     */
    @Named("appTypeToName")
    default String appTypeToName(AppType type) {
        return type != null ? type.getName() : null;
    }
    
    /**
     * 应用类型枚举转描述
     */
    @Named("appTypeToDescription")
    default String appTypeToDescription(AppType type) {
        return type != null ? type.getDescription() : null;
    }
    
    /**
     * 应用状态枚举转字符串
     */
    @Named("appStatusToString")
    default String appStatusToString(AppStatus status) {
        return status != null ? status.name() : null;
    }
    
    /**
     * 应用状态枚举转名称
     */
    @Named("appStatusToName")
    default String appStatusToName(AppStatus status) {
        return status != null ? status.getName() : null;
    }
    
    /**
     * 领域对象转可用状态
     */
    @Named("domainToUsable")
    default Boolean domainToUsable(AiApp aiApp) {
        return aiApp != null ? aiApp.isUsable() : false;
    }
    
    /**
     * 领域对象转可编辑状态
     */
    @Named("domainToEditable")
    default Boolean domainToEditable(AiApp aiApp) {
        return aiApp != null ? aiApp.isEditable() : false;
    }
}