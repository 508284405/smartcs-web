package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentRoute;
import com.leyue.smartcs.intent.dataobject.IntentRouteDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图路由数据对象转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentRouteConvertor {
    
    /**
     * DO转Domain
     * @param routeDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "routeType", expression = "java(com.leyue.smartcs.domain.intent.enums.RouteType.fromCode(routeDO.getRouteType()))")
    IntentRoute toDomain(IntentRouteDO routeDO);
    
    /**
     * Domain转DO
     * @param route 领域对象
     * @return 数据对象
     */
    @Mapping(target = "routeType", expression = "java(route.getRouteType() != null ? route.getRouteType().getCode() : null)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    IntentRouteDO toDO(IntentRoute route);
    
    /**
     * DO列表转Domain列表
     * @param routeDOList 数据对象列表
     * @return 领域对象列表
     */
    List<IntentRoute> toDomainList(List<IntentRouteDO> routeDOList);
    
    /**
     * Domain列表转DO列表
     * @param routeList 领域对象列表
     * @return 数据对象列表
     */
    List<IntentRouteDO> toDOList(List<IntentRoute> routeList);
}