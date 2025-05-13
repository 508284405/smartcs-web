package com.leyue.smartcs.bot.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.bot.convertor.PromptTemplateConvertor;
import com.leyue.smartcs.bot.dataobject.BotPromptTemplateDO;
import com.leyue.smartcs.bot.mapper.BotPromptTemplateMapper;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import com.leyue.smartcs.domain.bot.model.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Prompt模板网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateGatewayImpl implements PromptTemplateGateway {
    
    private final BotPromptTemplateMapper botPromptTemplateMapper;
    
    @Override
    public PromptTemplate save(PromptTemplate promptTemplate) {
        BotPromptTemplateDO botPromptTemplateDO = PromptTemplateConvertor.INSTANCE.toDO(promptTemplate);
        
        if (botPromptTemplateDO.getId() != null) {
            botPromptTemplateMapper.updateById(botPromptTemplateDO);
        } else {
            botPromptTemplateMapper.insert(botPromptTemplateDO);
        }
        
        return PromptTemplateConvertor.INSTANCE.toEntity(botPromptTemplateDO);
    }
    
    @Override
    public Optional<PromptTemplate> findById(Long id) {
        BotPromptTemplateDO botPromptTemplateDO = botPromptTemplateMapper.selectById(id);
        return Optional.ofNullable(botPromptTemplateDO)
                .map(PromptTemplateConvertor.INSTANCE::toEntity);
    }
    
    @Override
    public Optional<PromptTemplate> findByTemplateKey(String templateKey) {
        LambdaQueryWrapper<BotPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotPromptTemplateDO::getTemplateKey, templateKey);
        wrapper.eq(BotPromptTemplateDO::getIsDeleted, 0);
        
        BotPromptTemplateDO botPromptTemplateDO = botPromptTemplateMapper.selectOne(wrapper);
        return Optional.ofNullable(botPromptTemplateDO)
                .map(PromptTemplateConvertor.INSTANCE::toEntity);
    }
    
    @Override
    public List<PromptTemplate> findAll() {
        LambdaQueryWrapper<BotPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotPromptTemplateDO::getIsDeleted, 0);
        
        List<BotPromptTemplateDO> botPromptTemplateDOs = botPromptTemplateMapper.selectList(wrapper);
        return PromptTemplateConvertor.INSTANCE.toEntities(botPromptTemplateDOs);
    }
    
    @Override
    public boolean delete(Long id) {
        int result = botPromptTemplateMapper.deleteById(id);
        return result > 0;
    }
} 