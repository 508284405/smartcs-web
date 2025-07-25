package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.ModelPromptTemplate;
import com.leyue.smartcs.dto.model.ModelPromptTemplateCreateCmd;
import com.leyue.smartcs.dto.model.ModelPromptTemplateDTO;
import org.mapstruct.Mapper;

/**
 * 模型Prompt模板应用层转换器
 */
@Mapper(componentModel = "spring")
public interface ModelPromptTemplateAppConvertor {
    
    /**
     * CreateCmd转领域对象
     */
    ModelPromptTemplate toDomain(ModelPromptTemplateCreateCmd cmd);
    
    /**
     * 领域对象转DTO
     */
    ModelPromptTemplateDTO toDTO(ModelPromptTemplate template);
    
    /**
     * DTO转领域对象
     */
    ModelPromptTemplate toDomain(ModelPromptTemplateDTO dto);
}