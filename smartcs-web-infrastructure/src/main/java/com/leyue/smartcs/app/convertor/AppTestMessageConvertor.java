package com.leyue.smartcs.app.convertor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.app.dataobject.AppTestMessageDO;
import com.leyue.smartcs.domain.app.entity.AppTestMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI应用测试消息转换器
 */
@Mapper(componentModel = "spring")
@Component
public interface AppTestMessageConvertor {
    
    /**
     * 实体转数据对象
     */
    @Mapping(source = "variables", target = "variables", qualifiedByName = "mapToString")
    @Mapping(source = "modelInfo", target = "modelInfo", qualifiedByName = "mapToString")
    @Mapping(source = "tokenUsage", target = "tokenUsage", qualifiedByName = "mapToString")
    @Mapping(source = "messageType", target = "messageType", qualifiedByName = "messageTypeToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "messageStatusToString")
    AppTestMessageDO toDataObject(AppTestMessage entity);
    
    /**
     * 数据对象转实体
     */
    @Mapping(source = "variables", target = "variables", qualifiedByName = "stringToMap")
    @Mapping(source = "modelInfo", target = "modelInfo", qualifiedByName = "stringToMap")
    @Mapping(source = "tokenUsage", target = "tokenUsage", qualifiedByName = "stringToMap")
    @Mapping(source = "messageType", target = "messageType", qualifiedByName = "stringToMessageType")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToMessageStatus")
    AppTestMessage toEntity(AppTestMessageDO dataObject);
    
    /**
     * Map转换为JSON字符串
     */
    @Named("mapToString")
    default String mapToString(Map<String, Object> map) {
        return map != null ? JSON.toJSONString(map) : null;
    }
    
    /**
     * JSON字符串转换为Map
     */
    @Named("stringToMap")
    default Map<String, Object> stringToMap(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(jsonString);
            return jsonObject;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 消息类型枚举转字符串
     */
    @Named("messageTypeToString")
    default String messageTypeToString(AppTestMessage.MessageType messageType) {
        return messageType != null ? messageType.getCode() : null;
    }
    
    /**
     * 字符串转消息类型枚举
     */
    @Named("stringToMessageType")
    default AppTestMessage.MessageType stringToMessageType(String messageType) {
        if (messageType == null || messageType.trim().isEmpty()) {
            return null;
        }
        try {
            return AppTestMessage.MessageType.fromCode(messageType);
        } catch (Exception e) {
            return AppTestMessage.MessageType.USER; // 默认值
        }
    }
    
    /**
     * 消息状态枚举转字符串
     */
    @Named("messageStatusToString")
    default String messageStatusToString(AppTestMessage.MessageStatus status) {
        return status != null ? status.getCode() : null;
    }
    
    /**
     * 字符串转消息状态枚举
     */
    @Named("stringToMessageStatus")
    default AppTestMessage.MessageStatus stringToMessageStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return AppTestMessage.MessageStatus.fromCode(status);
        } catch (Exception e) {
            return AppTestMessage.MessageStatus.SUCCESS; // 默认值
        }
    }
}