package com.leyue.smartcs.domain.knowledge.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseListQry;

/**
 * 知识库Gateway接口
 */
public interface KnowledgeBaseGateway {
    
    /**
     * 保存知识库
     * @param knowledgeBase 知识库对象
     */
    KnowledgeBase save(KnowledgeBase knowledgeBase);
    
    /**
     * 更新知识库
     * @param knowledgeBase 知识库对象
     */
    void update(KnowledgeBase knowledgeBase);
    
    /**
     * 根据ID查找知识库
     * @param id 知识库ID
     * @return 知识库对象
     */
    KnowledgeBase findById(Long id);
    
    /**
     * 根据ID删除知识库
     * @param id 知识库ID
     */
    void deleteById(Long id);

    /**
     * 根据名称检查知识库是否存在
     * @param name 知识库名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 分页查询知识库列表
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<KnowledgeBase> listByPage(KnowledgeBaseListQry qry);
} 