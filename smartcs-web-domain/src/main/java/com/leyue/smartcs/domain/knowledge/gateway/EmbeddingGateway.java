package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.dto.knowledge.enums.StrategyNameEnum;

import java.util.List;
import java.util.Optional;

/**
 * 向量存储网关接口
 */
public interface EmbeddingGateway {
    /**
     * 保存向量
     * @param embedding 向量实体
     * @return 保存后的向量
     */
    Embedding save(Embedding embedding);
    
    /**
     * 批量保存向量
     * @param embeddings 向量列表
     * @return 保存后的向量列表
     */
    List<Embedding> saveBatch(List<Embedding> embeddings);
    
    /**
     * 根据ID查询向量
     * @param id 向量ID
     * @return 向量实体(可能为空)
     */
    Optional<Embedding> findById(Long id);
    
    /**
     * 根据文档ID查询向量列表
     * @param docId 文档ID
     * @return 向量列表
     */
    List<Embedding> findByDocId(Long docId);
    
    /**
     * 根据文档ID和段落序号查询向量
     * @param docId 文档ID
     * @param sectionIdx 段落序号
     * @return 向量实体(可能为空)
     */
    Optional<Embedding> findByDocIdAndSectionIdx(Long docId, Integer sectionIdx);
    
    /**
     * 根据文档ID和策略名称删除向量
     * @param docId 文档ID
     * @param strategyName 解析策略名称
     * @return 是否成功
     */
    boolean deleteByDocId(Long docId, StrategyNameEnum strategyName);
} 