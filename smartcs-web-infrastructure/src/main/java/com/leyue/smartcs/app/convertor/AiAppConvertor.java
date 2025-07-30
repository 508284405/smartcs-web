package com.leyue.smartcs.app.convertor;

import com.leyue.smartcs.app.dao.AiAppDO;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * AI应用转换器
 */
@Mapper
public interface AiAppConvertor {
    
    AiAppConvertor INSTANCE = Mappers.getMapper(AiAppConvertor.class);
    
    /**
     * DO转领域对象
     */
    @Mapping(source = "type", target = "type", qualifiedByName = "stringToAppType")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToAppStatus")
    AiApp doToDomain(AiAppDO aiAppDO);
    
    /**
     * 领域对象转DO
     */
    @Mapping(source = "type", target = "type", qualifiedByName = "appTypeToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "appStatusToString")
    AiAppDO domainToDo(AiApp aiApp);
    
    /**
     * 字符串转应用类型枚举
     */
    @Named("stringToAppType")
    default AppType stringToAppType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return AppType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 应用类型枚举转字符串
     */
    @Named("appTypeToString")
    default String appTypeToString(AppType type) {
        return type != null ? type.name() : null;
    }
    
    /**
     * 字符串转应用状态枚举
     */
    @Named("stringToAppStatus")
    default AppStatus stringToAppStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return AppStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 应用状态枚举转字符串
     */
    @Named("appStatusToString")
    default String appStatusToString(AppStatus status) {
        return status != null ? status.name() : null;
    }
}