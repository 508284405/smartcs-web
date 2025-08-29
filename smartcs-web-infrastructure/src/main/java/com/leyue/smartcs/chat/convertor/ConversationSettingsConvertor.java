package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.ConversationSettingsDO;
import com.leyue.smartcs.domain.chat.ConversationSettings;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会话设置转换器
 */
@Mapper
public interface ConversationSettingsConvertor {
    
    ConversationSettingsConvertor INSTANCE = Mappers.getMapper(ConversationSettingsConvertor.class);
    
    /**
     * DO转领域对象
     */
    ConversationSettings toDomain(ConversationSettingsDO settingsDO);
    
    /**
     * 领域对象转DO
     */
    ConversationSettingsDO toDO(ConversationSettings settings);
    
    /**
     * DO列表转领域对象列表
     */
    List<ConversationSettings> toDomainList(List<ConversationSettingsDO> settingsDOList);
    
    /**
     * 领域对象列表转DO列表
     */
    List<ConversationSettingsDO> toDOList(List<ConversationSettings> settingsList);
}