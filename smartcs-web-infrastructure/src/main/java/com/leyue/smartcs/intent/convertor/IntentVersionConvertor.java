package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.intent.dataobject.IntentVersionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图版本数据对象转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentVersionConvertor {
    
    /**
     * DO转Domain
     * @param versionDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "isDeleted", expression = "java(versionDO.getIsDeleted() != null && versionDO.getIsDeleted() == 1)")
    @Mapping(target = "status", expression = "java(com.leyue.smartcs.domain.intent.enums.VersionStatus.fromCode(versionDO.getStatus()))")
    @Mapping(target = "createdBy", source = "createdById")
    @Mapping(target = "approvedBy", source = "approvedById")
    IntentVersion toDomain(IntentVersionDO versionDO);
    
    /**
     * Domain转DO
     * @param version 领域对象
     * @return 数据对象
     */
    @Mapping(target = "isDeleted", expression = "java(version.getIsDeleted() != null && version.getIsDeleted() ? 1 : 0)")
    @Mapping(target = "status", expression = "java(version.getStatus() != null ? version.getStatus().getCode() : null)")
    @Mapping(target = "createdById", source = "createdBy")
    @Mapping(target = "approvedById", source = "approvedBy")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    IntentVersionDO toDO(IntentVersion version);
    
    /**
     * DO列表转Domain列表
     * @param versionDOList 数据对象列表
     * @return 领域对象列表
     */
    List<IntentVersion> toDomainList(List<IntentVersionDO> versionDOList);
    
    /**
     * Domain列表转DO列表
     * @param versionList 领域对象列表
     * @return 数据对象列表
     */
    List<IntentVersionDO> toDOList(List<IntentVersion> versionList);
}