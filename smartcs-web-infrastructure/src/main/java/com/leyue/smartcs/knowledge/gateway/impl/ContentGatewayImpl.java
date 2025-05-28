package com.leyue.smartcs.knowledge.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.knowledge.convertor.ContentConvertor;
import com.leyue.smartcs.knowledge.dataobject.ContentDO;
import com.leyue.smartcs.knowledge.mapper.ContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 内容Gateway实现
 */
@Component
public class ContentGatewayImpl implements ContentGateway {
    
    @Autowired
    private ContentMapper contentMapper;
    
    @Override
    public void save(Content content) {
        contentMapper.insert(ContentConvertor.INSTANCE.toDO(content));
    }
    
    @Override
    public void update(Content content) {
        contentMapper.updateById(ContentConvertor.INSTANCE.toDO(content));
    }
    
    @Override
    public Content findById(Long id) {
        return ContentConvertor.INSTANCE.toDomain(contentMapper.selectById(id));
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