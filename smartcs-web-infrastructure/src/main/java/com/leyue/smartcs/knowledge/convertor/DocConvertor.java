package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.knowledge.dataobject.DocDO;
import com.leyue.smartcs.dto.knowledge.DocDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文档对象转换接口
 */
@Mapper(componentModel = "spring")
public interface DocConvertor {

    DocConvertor INSTANCE = Mappers.getMapper(DocConvertor.class);
    
    /**
     * 将领域模型转换为数据对象
     */
    @Mapping(source = "version", target = "version")
    DocDO toDataObject(Document document);
    
    /**
     * 将数据对象转换为领域模型
     */
    @Mapping(source = "version", target = "version")
    Document toDomain(DocDO docDO);
    
    /**
     * 将领域模型转换为DTO
     */
    @Mapping(source = "version", target = "version")
    DocDTO toDTO(Document document);
    
    /**
     * 批量将数据对象转换为领域模型
     */
    List<Document> toDomainList(List<DocDO> docDOs);
    
    /**
     * 批量将领域模型转换为DTO
     */
    List<DocDTO> toDTOList(List<Document> documents);
} 