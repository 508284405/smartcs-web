package com.leyue.smartcs.chat.database.converter;

import com.leyue.smartcs.chat.database.dataobject.CsMessageDO;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.MessageType;
import com.leyue.smartcs.domain.chat.SenderRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * 消息数据转换器
 */
@Mapper(componentModel = "spring")
public interface MessageConverter {
    
    /**
     * 领域模型转数据对象
     *
     * @param message 消息领域模型
     * @return 消息数据对象
     */
    @Mapping(source = "senderRole", target = "senderRole", qualifiedByName = "senderRoleToCode")
    @Mapping(source = "msgType", target = "msgType", qualifiedByName = "messageTypeToCode")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CsMessageDO toDataObject(Message message);
    
    /**
     * 数据对象转领域模型
     *
     * @param csMessageDO 消息数据对象
     * @return 消息领域模型
     */
    @Mapping(source = "senderRole", target = "senderRole", qualifiedByName = "codeToSenderRole")
    @Mapping(source = "msgType", target = "msgType", qualifiedByName = "codeToMessageType")
    @Mapping(source = "createdAt", target = "createdAt")
    Message toDomain(CsMessageDO csMessageDO);
    
    /**
     * 更新存在的消息对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    @Mapping(source = "senderRole", target = "senderRole", qualifiedByName = "senderRoleToCode")
    @Mapping(source = "msgType", target = "msgType", qualifiedByName = "messageTypeToCode")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDataObject(Message source, @MappingTarget CsMessageDO target);
    
    /**
     * 发送者角色枚举转换为整数代码
     */
    @Named("senderRoleToCode")
    default Integer senderRoleToCode(SenderRole senderRole) {
        return senderRole != null ? senderRole.getCode() : null;
    }
    
    /**
     * 整数代码转换为发送者角色枚举
     */
    @Named("codeToSenderRole")
    default SenderRole codeToSenderRole(Integer code) {
        return code != null ? SenderRole.fromCode(code) : null;
    }
    
    /**
     * 消息类型枚举转换为整数代码
     */
    @Named("messageTypeToCode")
    default Integer messageTypeToCode(MessageType messageType) {
        return messageType != null ? messageType.getCode() : null;
    }
    
    /**
     * 整数代码转换为消息类型枚举
     */
    @Named("codeToMessageType")
    default MessageType codeToMessageType(Integer code) {
        return code != null ? MessageType.fromCode(code) : null;
    }
}
