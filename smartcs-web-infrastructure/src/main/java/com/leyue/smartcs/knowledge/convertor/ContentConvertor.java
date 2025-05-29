package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.knowledge.dataobject.ContentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 内容数据对象转换器
 */
@Mapper(componentModel = "spring")
public interface ContentConvertor {    
    /**
     * DO转Domain
     * @param contentDO 数据对象
     * @return 领域对象
     */
    Content toDomain(ContentDO contentDO);
    
    /**
     * Domain转DO
     * @param content 领域对象
     * @return 数据对象
     */
    ContentDO toDO(Content content);

    /**
     * 将领域模型转换为DTO
     */
    ContentDTO toDTO(Content content);

    ContentDTO toDTO(ContentDO contentDO);

    List<ContentDTO> toDTO(List<ContentDO> contentDOs);

    /**
     * 批量将数据对象转换为领域模型
     */
    List<Content> toDomainList(List<ContentDO> contentDOs);

    /**
     * 批量将领域模型转换为DTO
     */
    List<ContentDTO> toDTOList(List<Content> contents);
} 