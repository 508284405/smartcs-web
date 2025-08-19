package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.intent.dataobject.IntentCatalogDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图目录数据对象转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentCatalogConvertor {
    
    /**
     * DO转Domain
     * @param catalogDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "isDeleted", expression = "java(catalogDO.getIsDeleted() != null && catalogDO.getIsDeleted() == 1)")
    IntentCatalog toDomain(IntentCatalogDO catalogDO);
    
    /**
     * Domain转DO
     * @param catalog 领域对象
     * @return 数据对象
     */
    @Mapping(target = "isDeleted", expression = "java(catalog.getIsDeleted() != null && catalog.getIsDeleted() ? 1 : 0)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    IntentCatalogDO toDO(IntentCatalog catalog);
    
    /**
     * DO列表转Domain列表
     * @param catalogDOList 数据对象列表
     * @return 领域对象列表
     */
    List<IntentCatalog> toDomainList(List<IntentCatalogDO> catalogDOList);
    
    /**
     * Domain列表转DO列表
     * @param catalogList 领域对象列表
     * @return 数据对象列表
     */
    List<IntentCatalogDO> toDOList(List<IntentCatalog> catalogList);
}