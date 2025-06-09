package com.leyue.smartcs.knowledge.convertor;

import org.mapstruct.Mapper;
import org.springframework.ai.document.Document;

import com.leyue.smartcs.dto.knowledge.DocumentSearchResultDTO;

/**
 * 文档搜索结果转换器
 */
@Mapper(componentModel = "spring")
public interface DocumentSearchConvertor {
    /**
     * 将Spring AI的Document转换为DocumentSearchResultDTO
     *
     * @param document     Spring AI的Document对象
     * @param chunkGateway 切片网关，用于查询关联的切片信息
     * @return DocumentSearchResultDTO
     */
    DocumentSearchResultDTO toDTO(Document document);
}