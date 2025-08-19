package com.leyue.smartcs.model.convertor;

import org.mapstruct.Mapper;

import com.leyue.smartcs.domain.model.ModelPromptTemplate;
import com.leyue.smartcs.model.dataobject.ModelPromptTemplateDO;

/**
 * 模型Prompt模板转换器
 */
@Mapper(componentModel = "spring")
public interface ModelPromptTemplateConvertor {
    
    /**
     * DO转领域对象
     */
    ModelPromptTemplate toDomain(ModelPromptTemplateDO modelPromptTemplateDO);
    
    /**
     * 领域对象转DO
     */
    ModelPromptTemplateDO toDO(ModelPromptTemplate modelPromptTemplate);
}