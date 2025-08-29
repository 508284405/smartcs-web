package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.MessageReactionDO;
import com.leyue.smartcs.domain.chat.MessageReaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 消息表情反应转换器
 */
@Mapper
public interface MessageReactionConvertor {
    
    MessageReactionConvertor INSTANCE = Mappers.getMapper(MessageReactionConvertor.class);
    
    /**
     * DO转领域模型
     */
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedBy", target = "updatedBy")
    MessageReaction toDomain(MessageReactionDO dataObject);
    
    /**
     * 领域模型转DO
     */
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedBy", target = "updatedBy")
    MessageReactionDO toDataObject(MessageReaction domain);
    
    /**
     * 批量转换
     */
    List<MessageReaction> toDomainList(List<MessageReactionDO> dataObjects);
    
    List<MessageReactionDO> toDataObjectList(List<MessageReaction> domains);
}