package com.leyue.smartcs.app.convertor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.app.dataobject.AppTestSessionDO;
import com.leyue.smartcs.domain.app.entity.AppTestSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI应用测试会话转换器
 */
@Mapper(componentModel = "spring")
@Component
public interface AppTestSessionConvertor {
    
    /**
     * 实体转数据对象
     */
    @Mapping(source = "sessionConfig", target = "sessionConfig", qualifiedByName = "mapToString")
    @Mapping(source = "sessionState", target = "sessionState", qualifiedByName = "sessionStateToString")
    AppTestSessionDO toDataObject(AppTestSession entity);
    
    /**
     * 数据对象转实体
     */
    @Mapping(source = "sessionConfig", target = "sessionConfig", qualifiedByName = "stringToMap")
    @Mapping(source = "sessionState", target = "sessionState", qualifiedByName = "stringToSessionState")
    AppTestSession toEntity(AppTestSessionDO dataObject);
    
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
     * 会话状态枚举转字符串
     */
    @Named("sessionStateToString")
    default String sessionStateToString(AppTestSession.SessionState sessionState) {
        return sessionState != null ? sessionState.getCode() : null;
    }
    
    /**
     * 字符串转会话状态枚举
     */
    @Named("stringToSessionState")
    default AppTestSession.SessionState stringToSessionState(String sessionState) {
        if (sessionState == null || sessionState.trim().isEmpty()) {
            return null;
        }
        try {
            return AppTestSession.SessionState.fromCode(sessionState);
        } catch (Exception e) {
            return AppTestSession.SessionState.ACTIVE; // 默认值
        }
    }
}