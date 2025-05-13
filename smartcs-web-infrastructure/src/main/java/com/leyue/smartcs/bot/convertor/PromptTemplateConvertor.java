package com.leyue.smartcs.bot.convertor;

import com.leyue.smartcs.bot.dataobject.BotPromptTemplateDO;
import com.leyue.smartcs.domain.bot.model.PromptTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Prompt模板转换器
 */
@Mapper
public interface PromptTemplateConvertor {
    
    PromptTemplateConvertor INSTANCE = Mappers.getMapper(PromptTemplateConvertor.class);
    
    /**
     * 将DO转换为领域模型
     * @param botPromptTemplateDO DO对象
     * @return 领域模型
     */
    PromptTemplate toEntity(BotPromptTemplateDO botPromptTemplateDO);
    
    /**
     * 将领域模型转换为DO
     * @param promptTemplate 领域模型
     * @return DO对象
     */
    BotPromptTemplateDO toDO(PromptTemplate promptTemplate);
    
    /**
     * 批量将DO转换为领域模型
     * @param botPromptTemplateDOs DO对象列表
     * @return 领域模型列表
     */
    List<PromptTemplate> toEntities(List<BotPromptTemplateDO> botPromptTemplateDOs);
} 