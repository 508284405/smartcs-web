package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.CsMessageDO;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.enums.MessageType;
import com.leyue.smartcs.domain.chat.enums.MessageSendStatus;
import com.leyue.smartcs.domain.chat.enums.SenderRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * 消息数据转换器
 */
@Mapper(componentModel = "spring")
public interface MessageConvertor {
    
    /**
     * 领域模型转数据对象
     *
     * @param message 消息领域模型
     * @return 消息数据对象
     */
    @Mapping(source = "isRecalled", target = "isRecalled", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isDeletedBySender", target = "isDeletedBySender", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isDeletedByReceiver", target = "isDeletedByReceiver", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isEdited", target = "isEdited", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isRead", target = "isRead", qualifiedByName = "booleanToInteger")
    CsMessageDO toDataObject(Message message);
    
    /**
     * 数据对象转领域模型
     *
     * @param csMessageDO 消息数据对象
     * @return 消息领域模型
     */
    @Mapping(source = "isRecalled", target = "isRecalled", qualifiedByName = "integerToBoolean")
    @Mapping(source = "isDeletedBySender", target = "isDeletedBySender", qualifiedByName = "integerToBoolean")
    @Mapping(source = "isDeletedByReceiver", target = "isDeletedByReceiver", qualifiedByName = "integerToBoolean")
    @Mapping(source = "isEdited", target = "isEdited", qualifiedByName = "integerToBoolean")
    @Mapping(source = "isRead", target = "isRead", qualifiedByName = "integerToBoolean")
    Message toDomain(CsMessageDO csMessageDO);
    
    /**
     * 更新存在的消息对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    @Mapping(source = "isRecalled", target = "isRecalled", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isDeletedBySender", target = "isDeletedBySender", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isDeletedByReceiver", target = "isDeletedByReceiver", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isEdited", target = "isEdited", qualifiedByName = "booleanToInteger")
    @Mapping(source = "isRead", target = "isRead", qualifiedByName = "booleanToInteger")
    void updateDataObject(Message source, @MappingTarget CsMessageDO target);
    
    /**
     * 消息类型枚举转换为整数代码
     */
    default Integer map(MessageType messageType) {
        return messageType != null ? messageType.getCode() : null;
    }
    
    /**
     * 整数代码转换为消息类型枚举
     */
    default MessageType map(Integer code) {
        return code != null ? MessageType.fromCode(code) : null;
    }
    
    /**
     * 消息发送状态枚举转换为整数代码
     */
    default Integer map(MessageSendStatus sendStatus) {
        return sendStatus != null ? sendStatus.getCode() : null;
    }
    
    /**
     * Boolean转换为Integer（用于撤回状态）
     */
    @Named("booleanToInteger")
    default Integer booleanToInteger(Boolean value) {
        if (value == null) return null;
        return value ? 1 : 0;
    }
    
    /**
     * Integer转换为Boolean（用于撤回状态）
     */
    @Named("integerToBoolean")
    default Boolean integerToBoolean(Integer value) {
        if (value == null) return null;
        return value == 1;
    }
    
}
