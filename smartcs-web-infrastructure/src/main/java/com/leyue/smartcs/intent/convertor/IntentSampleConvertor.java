package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentSample;
import com.leyue.smartcs.intent.dataobject.IntentSampleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图样本数据对象转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentSampleConvertor {
    
    /**
     * DO转Domain
     * @param sampleDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "type", expression = "java(com.leyue.smartcs.domain.intent.enums.SampleType.fromCode(sampleDO.getType()))")
    @Mapping(target = "isDeleted", expression = "java(sampleDO.getIsDeleted() != null && sampleDO.getIsDeleted() == 1)")
    IntentSample toDomain(IntentSampleDO sampleDO);
    
    /**
     * Domain转DO
     * @param sample 领域对象
     * @return 数据对象
     */
    @Mapping(target = "type", expression = "java(sample.getType() != null ? sample.getType().getCode() : null)")
    @Mapping(target = "isDeleted", expression = "java(sample.getIsDeleted() != null && sample.getIsDeleted() ? 1 : 0)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    IntentSampleDO toDO(IntentSample sample);
    
    /**
     * DO列表转Domain列表
     * @param sampleDOList 数据对象列表
     * @return 领域对象列表
     */
    List<IntentSample> toDomainList(List<IntentSampleDO> sampleDOList);
    
    /**
     * Domain列表转DO列表
     * @param sampleList 领域对象列表
     * @return 数据对象列表
     */
    List<IntentSampleDO> toDOList(List<IntentSample> sampleList);
}