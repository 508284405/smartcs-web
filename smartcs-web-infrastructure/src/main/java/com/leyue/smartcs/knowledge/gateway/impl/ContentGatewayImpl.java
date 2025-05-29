package com.leyue.smartcs.knowledge.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.knowledge.convertor.ContentConvertor;
import com.leyue.smartcs.knowledge.dataobject.ContentDO;
import com.leyue.smartcs.knowledge.mapper.ContentMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 内容Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ContentGatewayImpl implements ContentGateway {
    
    private final ContentMapper contentMapper;

    private final ContentConvertor contentConvertor;
    
    @Override
    public void save(Content content) {
        contentMapper.insert(contentConvertor.toDO(content));
    }
    
    @Override
    public void update(Content content) {
        contentMapper.updateById(contentConvertor.toDO(content));
    }
    
    @Override
    public Content findById(Long id) {
        return contentConvertor.toDomain(contentMapper.selectById(id));
    }
    
    @Override
    public void deleteById(Long id) {
        contentMapper.deleteById(id);
    }

    @Override
    public Long countByKnowledgeBaseId(Long knowledgeBaseId) {
        return contentMapper.selectCount(new LambdaQueryWrapper<ContentDO>()
                .eq(ContentDO::getKnowledgeBaseId, knowledgeBaseId));
    }
} 