package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.SegmentMode;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.knowledge.dataobject.ContentDO;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
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

    default ContentStatusEnum map(String status){
        if (status == null)
            return null;
        return ContentStatusEnum.valueOf(status);
    }

    default SegmentMode mapSegmentMode(String segmentMode) {
        if (segmentMode == null)
            return null;
        return SegmentMode.fromCode(segmentMode);
    }

    /**
     * 计算召回率百分比
     */
    default Double calculateRecallRate(Content content) {
        if (content == null || content.getChunkCount() == null || content.getChunkCount() == 0) {
            return 0.0;
        }
        return (content.getRecallCount() != null ? content.getRecallCount() : 0) * 100.0 / content.getChunkCount();
    }
} 