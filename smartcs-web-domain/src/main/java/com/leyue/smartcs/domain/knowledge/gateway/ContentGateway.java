package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.Content;

/**
 * 内容Gateway接口
 */
public interface ContentGateway {
    
    /**
     * 保存内容
     * @param content 内容对象
     */
    void save(Content content);
    
    /**
     * 更新内容
     * @param content 内容对象
     */
    void update(Content content);
    
    /**
     * 根据ID查找内容
     * @param id 内容ID
     * @return 内容对象
     */
    Content findById(Long id);
    
    /**
     * 根据ID删除内容
     * @param id 内容ID
     */
    void deleteById(Long id);

    /**
     * 根据知识库ID统计内容数量
     * @param knowledgeBaseId 知识库ID
     * @return 内容数量
     */
    Long countByKnowledgeBaseId(Long knowledgeBaseId);
} 