package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.UserKnowledgeBaseRel;

/**
 * 用户知识库权限关系Gateway接口
 */
public interface UserKnowledgeBaseRelGateway {
    
    /**
     * 保存用户知识库权限关系
     * @param userKnowledgeBaseRel 用户知识库权限关系对象
     */
    void save(UserKnowledgeBaseRel userKnowledgeBaseRel);
    
    /**
     * 更新用户知识库权限关系
     * @param userKnowledgeBaseRel 用户知识库权限关系对象
     */
    void update(UserKnowledgeBaseRel userKnowledgeBaseRel);
    
    /**
     * 根据ID查找用户知识库权限关系
     * @param id 权限关系ID
     * @return 用户知识库权限关系对象
     */
    UserKnowledgeBaseRel findById(Long id);
    
    /**
     * 根据ID删除用户知识库权限关系
     * @param id 权限关系ID
     */
    void deleteById(Long id);
} 