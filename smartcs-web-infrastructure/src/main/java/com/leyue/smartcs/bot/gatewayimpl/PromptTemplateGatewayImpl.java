package com.leyue.smartcs.bot.gatewayimpl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.bot.convertor.PromptTemplateConvertor;
import com.leyue.smartcs.bot.dataobject.BotPromptTemplateDO;
import com.leyue.smartcs.bot.dto.BotPromptTemplatePageQry;
import com.leyue.smartcs.bot.mapper.BotProfileMapper;
import com.leyue.smartcs.bot.mapper.BotPromptTemplateMapper;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prompt模板网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateGatewayImpl implements PromptTemplateGateway {
    
    private final BotPromptTemplateMapper botPromptTemplateMapper;
    private final BotProfileMapper botProfileMapper;
    private final PromptTemplateConvertor promptTemplateConvertor;
    
    @Override
    public PromptTemplate save(PromptTemplate promptTemplate) {
        BotPromptTemplateDO botPromptTemplateDO = promptTemplateConvertor.toDO(promptTemplate);
        
        if (botPromptTemplateDO.getId() != null) {
            botPromptTemplateMapper.updateById(botPromptTemplateDO);
        } else {
            botPromptTemplateMapper.insert(botPromptTemplateDO);
        }
        
        return promptTemplateConvertor.toDomain(botPromptTemplateDO);
    }
    
    @Override
    public Optional<PromptTemplate> findById(Long id) {
        BotPromptTemplateDO botPromptTemplateDO = botPromptTemplateMapper.selectById(id);
        return Optional.ofNullable(botPromptTemplateDO)
                .map(promptTemplateConvertor::toDomain);
    }
    
    @Override
    public Optional<PromptTemplate> findByTemplateKey(String templateKey) {
        LambdaQueryWrapper<BotPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotPromptTemplateDO::getTemplateKey, templateKey);
        wrapper.eq(BotPromptTemplateDO::getIsDeleted, 0);
        
        BotPromptTemplateDO botPromptTemplateDO = botPromptTemplateMapper.selectOne(wrapper);
        return Optional.ofNullable(botPromptTemplateDO)
                .map(promptTemplateConvertor::toDomain);
    }
    
    @Override
    public List<PromptTemplate> findAll() {
        LambdaQueryWrapper<BotPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotPromptTemplateDO::getIsDeleted, 0);
        
        List<BotPromptTemplateDO> botPromptTemplateDOs = botPromptTemplateMapper.selectList(wrapper);
        return botPromptTemplateDOs.stream()
                .map(promptTemplateConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean delete(Long id) {
        int result = botPromptTemplateMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public PageResponse<PromptTemplate> pageQuery(BotPromptTemplatePageQry qry) {
        LambdaQueryWrapper<BotPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(qry.getTemplateKey())) {
            wrapper.like(BotPromptTemplateDO::getTemplateKey, qry.getTemplateKey());
        }
        wrapper.eq(BotPromptTemplateDO::getIsDeleted, 0);
        wrapper.orderByDesc(BotPromptTemplateDO::getCreatedAt);
        
        Page<BotPromptTemplateDO> page = new Page<>(qry.getPageIndex(), qry.getPageSize());
        Page<BotPromptTemplateDO> result = botPromptTemplateMapper.selectPage(page, wrapper);
        
        List<PromptTemplate> templates = result.getRecords().stream()
                .map(promptTemplateConvertor::toDomain)
                .collect(Collectors.toList());
        
        return PageResponse.of(templates, (int) result.getTotal(), qry.getPageSize(), qry.getPageIndex());
    }
    
    @Override
    public boolean isTemplateInUse(String templateKey) {
        int count = botProfileMapper.countByPromptKey(templateKey);
        return count > 0;
    }
} 