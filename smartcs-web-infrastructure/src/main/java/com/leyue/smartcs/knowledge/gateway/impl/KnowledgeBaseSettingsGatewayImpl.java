package com.leyue.smartcs.knowledge.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.knowledge.KnowledgeBaseSettings;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseSettingsGateway;
import com.leyue.smartcs.knowledge.convertor.KnowledgeBaseSettingsConvertor;
import com.leyue.smartcs.knowledge.dataobject.KnowledgeBaseSettingsDO;
import com.leyue.smartcs.knowledge.mapper.KnowledgeBaseSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 知识库设置Gateway实现
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseSettingsGatewayImpl implements KnowledgeBaseSettingsGateway {
    
    private final KnowledgeBaseSettingsMapper settingsMapper;
    
    private final KnowledgeBaseSettingsConvertor settingsConvertor;
    
    @Override
    public KnowledgeBaseSettings findByKnowledgeBaseId(Long knowledgeBaseId) {
        KnowledgeBaseSettingsDO settingsDO = settingsMapper.selectByKnowledgeBaseId(knowledgeBaseId);
        return settingsDO != null ? settingsConvertor.toDomain(settingsDO) : null;
    }
    
    @Override
    public KnowledgeBaseSettings saveOrUpdate(KnowledgeBaseSettings settings) {
        KnowledgeBaseSettingsDO settingsDO = settingsConvertor.toDO(settings);
        
        // 检查是否已存在该知识库的设置
        KnowledgeBaseSettingsDO existingSettings = settingsMapper.selectByKnowledgeBaseId(settings.getKnowledgeBaseId());
        
        if (existingSettings == null) {
            // 新增
            settingsMapper.insert(settingsDO);
            log.info("知识库设置新增成功, knowledgeBaseId: {}", settings.getKnowledgeBaseId());
        } else {
            // 更新，使用现有的ID
            settingsDO.setId(existingSettings.getId());
            settingsMapper.updateByKnowledgeBaseId(settingsDO);
            log.info("知识库设置更新成功, knowledgeBaseId: {}", settings.getKnowledgeBaseId());
        }
        
        return settingsConvertor.toDomain(settingsDO);
    }
    
    @Override
    public void deleteByKnowledgeBaseId(Long knowledgeBaseId) {
        settingsMapper.delete(new LambdaQueryWrapper<KnowledgeBaseSettingsDO>()
            .eq(KnowledgeBaseSettingsDO::getKnowledgeBaseId, knowledgeBaseId));
        log.info("知识库设置删除成功, knowledgeBaseId: {}", knowledgeBaseId);
    }
}