package com.leyue.smartcs.chat.database.converter;

import com.leyue.smartcs.chat.database.dataobject.CsSessionDO;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.SessionState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * 会话数据转换器
 */
@Mapper(componentModel = "spring")
public interface SessionConverter {
    
    /**
     * 领域模型转数据对象
     *
     * @param session 会话领域模型
     * @return 会话数据对象
     */
    @Mapping(source = "sessionState", target = "sessionState", qualifiedByName = "sessionStateToCode")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastMsgTime", target = "lastMsgTime")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CsSessionDO toDataObject(Session session);
    
    /**
     * 数据对象转领域模型
     *
     * @param csSessionDO 会话数据对象
     * @return 会话领域模型
     */
    @Mapping(source = "sessionState", target = "sessionState", qualifiedByName = "codeToSessionState")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastMsgTime", target = "lastMsgTime")
    Session toDomain(CsSessionDO csSessionDO);
    
    /**
     * 更新存在的对象
     * 
     * @param source 源对象
     * @param target 目标对象
     */
    @Mapping(source = "sessionState", target = "sessionState", qualifiedByName = "sessionStateToCode")
    @Mapping(source = "lastMsgTime", target = "lastMsgTime")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDataObject(Session source, @MappingTarget CsSessionDO target);
    
    /**
     * 会话状态枚举转换为整数代码
     */
    @Named("sessionStateToCode")
    default Integer sessionStateToCode(SessionState sessionState) {
        return sessionState != null ? sessionState.getCode() : null;
    }
    
    /**
     * 整数代码转换为会话状态枚举
     */
    @Named("codeToSessionState")
    default SessionState codeToSessionState(Integer code) {
        return code != null ? SessionState.fromCode(code) : null;
    }
}
