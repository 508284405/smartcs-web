package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.intent.dataobject.IntentDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图数据对象转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentConvertor {
    
    /**
     * DO转Domain
     * @param intentDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "isDeleted", expression = "java(intentDO.getIsDeleted() != null && intentDO.getIsDeleted() == 1)")
    @Mapping(target = "status", expression = "java(com.leyue.smartcs.domain.intent.enums.IntentStatus.fromCode(intentDO.getStatus()))")
    Intent toDomain(IntentDO intentDO);
    
    /**
     * Domain转DO
     * @param intent 领域对象
     * @return 数据对象
     */
    @Mapping(target = "isDeleted", expression = "java(intent.getIsDeleted() != null && intent.getIsDeleted() ? 1 : 0)")
    @Mapping(target = "status", expression = "java(intent.getStatus() != null ? intent.getStatus().getCode() : null)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    IntentDO toDO(Intent intent);
    
    /**
     * DO列表转Domain列表
     * @param intentDOList 数据对象列表
     * @return 领域对象列表
     */
    List<Intent> toDomainList(List<IntentDO> intentDOList);
    
    /**
     * Domain列表转DO列表
     * @param intentList 领域对象列表
     * @return 数据对象列表
     */
    List<IntentDO> toDOList(List<Intent> intentList);
}