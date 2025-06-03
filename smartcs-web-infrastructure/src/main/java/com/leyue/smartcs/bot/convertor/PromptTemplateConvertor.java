package com.leyue.smartcs.bot.convertor;

import org.mapstruct.Mapper;

import com.leyue.smartcs.bot.dataobject.BotPromptTemplateDO;
import com.leyue.smartcs.bot.dto.BotPromptTemplateDTO;
import com.leyue.smartcs.domain.bot.PromptTemplate;

/**
 * Prompt模板转换器
 */
@Mapper(componentModel = "spring")
public interface PromptTemplateConvertor {
    /**
     * 将领域模型转换为DO
     * @param promptTemplate 领域模型
     * @return DO对象
     */
    BotPromptTemplateDO toDO(PromptTemplate promptTemplate);

    /**
     * DO转换为PromptTemplate领域对象
     */
    PromptTemplate toDomain(BotPromptTemplateDO dataObject);
    
    /**
     * PromptTemplate领域对象转换为DTO
     */
    BotPromptTemplateDTO toDTO(PromptTemplate domain);
    
    /**
     * DTO转换为PromptTemplate领域对象
     */
    PromptTemplate toDomain(BotPromptTemplateDTO dto);
} 