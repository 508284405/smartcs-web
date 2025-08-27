package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.dto.intent.IntentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图应用层转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentAppConvertor {
    
    /**
     * 将意图领域对象转换为DTO
     */
    @Mapping(target = "status", expression = "java(intent.getStatus() != null ? intent.getStatus().getCode() : null)")
    @Mapping(target = "catalogName", ignore = true)
    @Mapping(target = "currentVersionNumber", ignore = true)
    IntentDTO toDTO(Intent intent);
    
    /**
     * 批量转换意图领域对象为DTO
     */
    List<IntentDTO> toDTOList(List<Intent> intents);
    
}