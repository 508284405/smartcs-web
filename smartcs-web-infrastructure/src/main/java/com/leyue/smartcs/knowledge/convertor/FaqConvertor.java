package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.model.Faq;
import com.leyue.smartcs.knowledge.dataobject.FaqDO;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * FAQ对象转换接口
 */
@Mapper(componentModel = "spring")
public interface FaqConvertor {

    FaqConvertor INSTANCE = Mappers.getMapper(FaqConvertor.class);
    
    /**
     * 将领域模型转换为数据对象
     */
    @Mapping(source = "version", target = "version")
    FaqDO toDataObject(Faq faq);
    
    /**
     * 将数据对象转换为领域模型
     */
    @Mapping(source = "version", target = "version")
    Faq toDomain(FaqDO faqDO);
    
    /**
     * 将领域模型转换为DTO
     */
    @Mapping(source = "version", target = "version")
    FaqDTO toDTO(Faq faq);
    
    /**
     * 批量将数据对象转换为领域模型
     */
    List<Faq> toDomainList(List<FaqDO> faqDOs);
    
    /**
     * 批量将领域模型转换为DTO
     */
    List<FaqDTO> toDTOList(List<Faq> faqs);
}