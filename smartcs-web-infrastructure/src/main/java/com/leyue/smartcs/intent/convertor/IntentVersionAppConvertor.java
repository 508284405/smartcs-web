package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.dto.intent.IntentVersionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图版本应用层转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentVersionAppConvertor {
    
    /**
     * 将意图版本领域对象转换为DTO
     */
    @Mapping(target = "status", expression = "java(version.getStatus() != null ? version.getStatus().getCode() : null)")
    IntentVersionDTO toDTO(IntentVersion version);
    
    /**
     * 批量转换意图版本领域对象为DTO
     */
    List<IntentVersionDTO> toDTOList(List<IntentVersion> versions);
    
}