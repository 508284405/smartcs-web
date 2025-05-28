package com.leyue.smartcs.knowledge.convertor;

import java.util.List;

import org.mapstruct.Mapper;

import com.leyue.smartcs.domain.knowledge.Faq;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.knowledge.dataobject.FaqDO;

/**
 * FAQ对象转换接口
 */
@Mapper(componentModel = "spring")
public interface FaqConvertor {
    /**
     * 将领域模型转换为数据对象
     */
    FaqDO toDataObject(Faq faq);

    /**
     * 将数据对象转换为领域模型
     */
    Faq toDomain(FaqDO faqDO);

    /**
     * 将领域模型转换为DTO
     */
    FaqDTO toDTO(Faq faq);

    FaqDTO toDTO(FaqDO faqDO);

    List<FaqDTO> toDTO(List<FaqDO> faqDO);

    /**
     * 批量将数据对象转换为领域模型
     */
    List<Faq> toDomainList(List<FaqDO> faqDOs);

    /**
     * 批量将领域模型转换为DTO
     */
    List<FaqDTO> toDTOList(List<Faq> faqs);
}