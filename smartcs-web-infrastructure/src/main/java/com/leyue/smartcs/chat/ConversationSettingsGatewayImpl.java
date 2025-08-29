package com.leyue.smartcs.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.chat.convertor.ConversationSettingsConvertor;
import com.leyue.smartcs.chat.dataobject.ConversationSettingsDO;
import com.leyue.smartcs.chat.mapper.ConversationSettingsMapper;
import com.leyue.smartcs.domain.chat.ConversationSettings;
import com.leyue.smartcs.domain.chat.gateway.ConversationSettingsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会话设置网关实现
 */
@Component
@RequiredArgsConstructor
public class ConversationSettingsGatewayImpl implements ConversationSettingsGateway {
    
    private final ConversationSettingsMapper conversationSettingsMapper;
    private final ConversationSettingsConvertor convertor = ConversationSettingsConvertor.INSTANCE;
    
    @Override
    public ConversationSettings save(ConversationSettings settings) {
        ConversationSettingsDO settingsDO = convertor.toDO(settings);
        
        if (settingsDO.getId() == null) {
            conversationSettingsMapper.insert(settingsDO);
        } else {
            conversationSettingsMapper.updateById(settingsDO);
        }
        
        return convertor.toDomain(settingsDO);
    }
    
    @Override
    public ConversationSettings findById(Long id) {
        ConversationSettingsDO settingsDO = conversationSettingsMapper.selectById(id);
        return settingsDO != null ? convertor.toDomain(settingsDO) : null;
    }
    
    @Override
    public ConversationSettings findByUserAndSession(String userId, Long sessionId) {
        ConversationSettingsDO settingsDO = conversationSettingsMapper.findByUserAndSession(userId, sessionId);
        return settingsDO != null ? convertor.toDomain(settingsDO) : null;
    }
    
    @Override
    public List<ConversationSettings> findByUserId(String userId) {
        List<ConversationSettingsDO> settingsDOList = conversationSettingsMapper.findByUserId(userId);
        return convertor.toDomainList(settingsDOList);
    }
    
    @Override
    public List<ConversationSettings> findPinnedConversations(String userId) {
        List<ConversationSettingsDO> settingsDOList = conversationSettingsMapper.findPinnedConversations(userId);
        return convertor.toDomainList(settingsDOList);
    }
    
    @Override
    public List<ConversationSettings> findMutedConversations(String userId) {
        List<ConversationSettingsDO> settingsDOList = conversationSettingsMapper.findMutedConversations(userId);
        return convertor.toDomainList(settingsDOList);
    }
    
    @Override
    public List<ConversationSettings> findArchivedConversations(String userId) {
        List<ConversationSettingsDO> settingsDOList = conversationSettingsMapper.findArchivedConversations(userId);
        return convertor.toDomainList(settingsDOList);
    }
    
    @Override
    public List<ConversationSettings> findActiveConversations(String userId) {
        List<ConversationSettingsDO> settingsDOList = conversationSettingsMapper.findActiveConversations(userId);
        return convertor.toDomainList(settingsDOList);
    }
    
    @Override
    public int updateExpiredMuteSettings() {
        return conversationSettingsMapper.updateExpiredMuteSettings();
    }
    
    @Override
    public long countPinnedConversations(String userId) {
        return conversationSettingsMapper.countPinnedConversations(userId);
    }
    
    @Override
    public long countArchivedConversations(String userId) {
        return conversationSettingsMapper.countArchivedConversations(userId);
    }
    
    @Override
    public boolean deleteById(Long id) {
        int affected = conversationSettingsMapper.deleteById(id);
        return affected > 0;
    }
    
    @Override
    public boolean deleteByUserAndSession(String userId, Long sessionId) {
        int affected = conversationSettingsMapper.deleteByUserAndSession(userId, sessionId);
        return affected > 0;
    }
    
    @Override
    public int deleteByUserId(String userId) {
        return conversationSettingsMapper.deleteByUserId(userId);
    }
}