package com.leyue.smartcs.knowledge.convertor;

import dev.langchain4j.data.document.Document;
import org.mapstruct.Mapper;

import com.leyue.smartcs.dto.knowledge.DocumentSearchResultDTO;

/**
 * 文档搜索结果转换器
 */
@Mapper(componentModel = "spring")
public interface DocumentSearchConvertor {
    DocumentSearchResultDTO toDTO(Document document);
}