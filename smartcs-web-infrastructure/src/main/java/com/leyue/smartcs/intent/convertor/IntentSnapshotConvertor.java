package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.enums.SnapshotStatus;
import com.leyue.smartcs.intent.dataobject.IntentSnapshotDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 意图快照转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentSnapshotConvertor {
    
    IntentSnapshotConvertor INSTANCE = Mappers.getMapper(IntentSnapshotConvertor.class);
    
    /**
     * 实体转数据对象
     */
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().getCode() : null)")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "publishedById", source = "publishedBy")
    @Mapping(target = "createdById", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "isDeleted", expression = "java(entity.getIsDeleted() != null && entity.getIsDeleted() ? 1 : 0)")
    IntentSnapshotDO toDataObject(IntentSnapshot entity);
    
    /**
     * 数据对象转实体
     */
    @Mapping(target = "status", expression = "java(dataObject.getStatus() != null ? com.leyue.smartcs.domain.intent.enums.SnapshotStatus.fromCode(dataObject.getStatus()) : null)")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "publishedBy", source = "publishedById")
    @Mapping(target = "createdBy", source = "createdById")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "isDeleted", expression = "java(dataObject.getIsDeleted() != null && dataObject.getIsDeleted() == 1)")
    @Mapping(target = "items", ignore = true) // 快照项目需要单独加载
    IntentSnapshot toEntity(IntentSnapshotDO dataObject);
}