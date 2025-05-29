package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.common.EmbeddingStructure;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * 统一搜索网关接口
 * 包含向量检索和全文检索功能
 */
public interface SearchGateway {
    
    // ========== 向量检索相关方法 ==========

    /**
     * 执行Top-K检索
     *
     * @return ID与相似度分数的映射
     */
    Map<Long, Double> searchTopK(String index, String keyword, int k, Long kbId, Long contentId);
    
    // ========== 全文检索相关方法 ==========
    
    /**
     * 关键词搜索
     *
     * @param index   索引名称
     * @param keyword 关键词
     * @param k       返回数量
     * @return ID与分数的映射
     */
    Map<Long, Double> searchByKeyword(String index, String keyword, int k);

    /**
     * 创建或更新索引文档
     *
     * @param index  索引名称
     * @param id     文档ID
     * @param source 文档内容
     * @return 是否成功
     */
    boolean indexDocument(String index, Long id, Object source);

    /**
     * 删除索引文档
     *
     * @param index 索引名称
     * @param id    文档ID
     * @return 是否成功
     */
    boolean deleteDocument(String index, Long id);
    
    // ========== 索引管理相关方法 ==========
    
    /**
     * 创建索引
     *
     * @param index      索引名称
     * @param fieldIndex 字段索引配置
     */
    void createIndex(String index, Object... fieldIndex);

    /**
     * 获取索引信息
     *
     * @param indexName 索引名称
     * @return 索引信息
     */
    IndexInfoDTO getIndexInfo(String indexName);

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @return 是否成功
     */
    boolean deleteIndex(String indexName);

    /**
     * 列出所有索引
     *
     * @return 索引名称列表
     */
    List<String> listIndexes();
} 