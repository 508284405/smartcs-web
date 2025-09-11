package com.leyue.smartcs.dictionary.convertor;

import com.leyue.smartcs.dictionary.dataobject.DictionaryEntryDO;
import com.leyue.smartcs.domain.dictionary.entity.DictionaryEntry;
import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import com.leyue.smartcs.domain.dictionary.enums.EntryStatus;
import com.leyue.smartcs.domain.dictionary.valueobject.DictionaryConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 字典实体转换器
 * 用于领域实体和数据对象之间的转换
 * 
 * @author Claude
 */
@Mapper
public interface DictionaryConvertor {
    
    DictionaryConvertor INSTANCE = Mappers.getMapper(DictionaryConvertor.class);
    
    /**
     * 领域实体转换为数据对象
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "dictionaryType", source = "dictionaryType", qualifiedByName = "dictionaryTypeToString")
    @Mapping(target = "tenant", source = "config.tenant")
    @Mapping(target = "channel", source = "config.channel")
    @Mapping(target = "domain", source = "config.domain")
    @Mapping(target = "entryKey", source = "entryKey")
    @Mapping(target = "entryValue", source = "entryValue")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status", qualifiedByName = "entryStatusToString")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "isDeleted", ignore = true) // 由MyBatis管理
    @Mapping(target = "createdAt", source = "createTime", qualifiedByName = "localDateTimeToTimestamp")
    @Mapping(target = "updatedAt", source = "updateTime", qualifiedByName = "localDateTimeToTimestamp")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    DictionaryEntryDO toDataObject(DictionaryEntry entity);
    
    /**
     * 数据对象转换为领域实体
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "dictionaryType", source = "dictionaryType", qualifiedByName = "stringToDictionaryType")
    @Mapping(target = "config", expression = "java(DictionaryConfig.of(dataObject.getTenant(), dataObject.getChannel(), dataObject.getDomain()))")
    @Mapping(target = "entryKey", source = "entryKey")
    @Mapping(target = "entryValue", source = "entryValue")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToEntryStatus")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "createTime", source = "createdAt", qualifiedByName = "timestampToLocalDateTime")
    @Mapping(target = "updateTime", source = "updatedAt", qualifiedByName = "timestampToLocalDateTime")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    DictionaryEntry toDomainEntity(DictionaryEntryDO dataObject);
    
    /**
     * 批量转换：领域实体列表转数据对象列表
     */
    List<DictionaryEntryDO> toDataObjectList(List<DictionaryEntry> entities);
    
    /**
     * 批量转换：数据对象列表转领域实体列表
     */
    List<DictionaryEntry> toDomainEntityList(List<DictionaryEntryDO> dataObjects);
    
    /**
     * 字典类型枚举转字符串
     */
    @Named("dictionaryTypeToString")
    default String dictionaryTypeToString(DictionaryType dictionaryType) {
        return dictionaryType != null ? dictionaryType.getCode() : null;
    }
    
    /**
     * 字符串转字典类型枚举
     */
    @Named("stringToDictionaryType")
    default DictionaryType stringToDictionaryType(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        try {
            return DictionaryType.fromCode(code);
        } catch (IllegalArgumentException e) {
            // 记录警告日志，但不抛出异常，避免数据转换失败
            return null;
        }
    }
    
    /**
     * 条目状态枚举转字符串
     */
    @Named("entryStatusToString")
    default String entryStatusToString(EntryStatus status) {
        return status != null ? status.getCode() : null;
    }
    
    /**
     * 字符串转条目状态枚举
     */
    @Named("stringToEntryStatus")
    default EntryStatus stringToEntryStatus(String code) {
        if (code == null || code.isEmpty()) {
            return EntryStatus.ACTIVE; // 默认状态
        }
        try {
            return EntryStatus.fromCode(code);
        } catch (IllegalArgumentException e) {
            // 记录警告日志，返回默认状态
            return EntryStatus.ACTIVE;
        }
    }
    
    /**
     * LocalDateTime转时间戳（毫秒）
     */
    @Named("localDateTimeToTimestamp")
    default Long localDateTimeToTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    
    /**
     * 时间戳（毫秒）转LocalDateTime
     */
    @Named("timestampToLocalDateTime")
    default LocalDateTime timestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }
}