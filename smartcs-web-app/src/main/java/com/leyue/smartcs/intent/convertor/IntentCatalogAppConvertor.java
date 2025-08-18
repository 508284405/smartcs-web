package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.dto.intent.IntentCatalogDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 意图目录应用层转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentCatalogAppConvertor {
    
    /**
     * 将意图目录领域对象转换为DTO
     */
    IntentCatalogDTO toDTO(IntentCatalog catalog);
    
    /**
     * 批量转换意图目录领域对象为DTO
     */
    List<IntentCatalogDTO> toDTOList(List<IntentCatalog> catalogs);
    
}