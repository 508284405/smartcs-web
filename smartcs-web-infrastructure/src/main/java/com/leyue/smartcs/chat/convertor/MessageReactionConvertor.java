package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.MessageReactionDO;
import com.leyue.smartcs.domain.chat.MessageReaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 消息表情反应转换器
 */
@Mapper(componentModel = "spring")
public interface MessageReactionConvertor {
    
    /**
     * DO转领域模型
     */
    MessageReaction toDomain(MessageReactionDO dataObject);
    
    /**
     * 领域模型转DO
     * 注意：BaseDO中的字段(createdAt, updatedAt, createdBy, updatedBy, isDeleted)通过MyBatis自动填充处理
     */
    MessageReactionDO toDataObject(MessageReaction domain);
    
    /**
     * 批量转换
     */
    List<MessageReaction> toDomainList(List<MessageReactionDO> dataObjects);
    
    List<MessageReactionDO> toDataObjectList(List<MessageReaction> domains);
}