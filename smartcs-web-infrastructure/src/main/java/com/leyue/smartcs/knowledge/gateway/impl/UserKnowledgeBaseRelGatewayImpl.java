package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.UserKnowledgeBaseRel;
import com.leyue.smartcs.domain.knowledge.gateway.UserKnowledgeBaseRelGateway;
import com.leyue.smartcs.knowledge.convertor.UserKnowledgeBaseRelConvertor;
import com.leyue.smartcs.knowledge.mapper.UserKnowledgeBaseRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户知识库权限关系Gateway实现
 */
@Component
public class UserKnowledgeBaseRelGatewayImpl implements UserKnowledgeBaseRelGateway {
    
    @Autowired
    private UserKnowledgeBaseRelMapper userKnowledgeBaseRelMapper;
    
    @Override
    public void save(UserKnowledgeBaseRel userKnowledgeBaseRel) {
        userKnowledgeBaseRelMapper.insert(UserKnowledgeBaseRelConvertor.INSTANCE.toDO(userKnowledgeBaseRel));
    }
    
    @Override
    public void update(UserKnowledgeBaseRel userKnowledgeBaseRel) {
        userKnowledgeBaseRelMapper.updateById(UserKnowledgeBaseRelConvertor.INSTANCE.toDO(userKnowledgeBaseRel));
    }
    
    @Override
    public UserKnowledgeBaseRel findById(Long id) {
        return UserKnowledgeBaseRelConvertor.INSTANCE.toDomain(userKnowledgeBaseRelMapper.selectById(id));
    }
    
    @Override
    public void deleteById(Long id) {
        userKnowledgeBaseRelMapper.deleteById(id);
    }
} 