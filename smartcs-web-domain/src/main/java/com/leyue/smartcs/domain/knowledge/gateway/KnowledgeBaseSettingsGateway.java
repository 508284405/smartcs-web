package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.KnowledgeBaseSettings;

/**
 * 知识库设置Gateway接口
 */
public interface KnowledgeBaseSettingsGateway {
    
    /**
     * 根据知识库ID查找设置
     * @param knowledgeBaseId 知识库ID
     * @return 知识库设置，如果不存在返回null
     */
    KnowledgeBaseSettings findByKnowledgeBaseId(Long knowledgeBaseId);
    
    /**
     * 保存或更新知识库设置
     * @param settings 知识库设置对象
     * @return 保存后的设置对象
     */
    KnowledgeBaseSettings saveOrUpdate(KnowledgeBaseSettings settings);
    
    /**
     * 根据知识库ID删除设置
     * @param knowledgeBaseId 知识库ID
     */
    void deleteByKnowledgeBaseId(Long knowledgeBaseId);
}