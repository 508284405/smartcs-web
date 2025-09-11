package com.leyue.smartcs.model.convertor;

import com.leyue.smartcs.domain.model.ModelContext;
import com.leyue.smartcs.dto.model.ModelContextDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型上下文应用层转换器
 */
@Mapper(componentModel = "spring")
public interface ModelContextAppConvertor {
    
    /**
     * 领域对象转DTO
     */
    @Mapping(target = "messages", expression = "java(convertMessagesToDTO(context.getMessagesList()))")
    ModelContextDTO toDTO(ModelContext context);
    
    /**
     * DTO转领域对象
     */
    @Mapping(target = "messages", expression = "java(convertMessagesToDomain(dto.getMessages()))")
    ModelContext toDomain(ModelContextDTO dto);
    
    /**
     * 转换消息列表到DTO
     */
    default List<ModelContextDTO.ContextMessage> convertMessagesToDTO(List<ModelContext.ContextMessage> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream()
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换消息到DTO
     */
    default ModelContextDTO.ContextMessage convertMessageToDTO(ModelContext.ContextMessage message) {
        if (message == null) {
            return null;
        }
        return ModelContextDTO.ContextMessage.builder()
                .role(message.getRole())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
    
    /**
     * 转换消息列表到领域对象
     */
    default String convertMessagesToDomain(List<ModelContextDTO.ContextMessage> messages) {
        if (messages == null) {
            return null;
        }
        List<ModelContext.ContextMessage> domainMessages = messages.stream()
                .map(this::convertMessageToDomain)
                .collect(Collectors.toList());
        
        // 这里需要通过ModelContext来设置消息列表，因为它会处理JSON序列化
        ModelContext tempContext = new ModelContext();
        tempContext.setMessagesList(domainMessages);
        return tempContext.getMessages();
    }
    
    /**
     * 转换消息到领域对象
     */
    default ModelContext.ContextMessage convertMessageToDomain(ModelContextDTO.ContextMessage message) {
        if (message == null) {
            return null;
        }
        return ModelContext.ContextMessage.builder()
                .role(message.getRole())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}