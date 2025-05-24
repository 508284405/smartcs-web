package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.model.Embedding;
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
     * 批量写入向量
     *
     * @param collection   集合名称
     * @param ids          ID列表
     * @param vectors      向量数据列表
     * @param partitionKey 分区键（可选）
     * @return 是否成功
     */
    boolean batchInsert(String collection, List<Embedding> embeddings);

    /**
     * 执行Top-K检索
     *
     * @param collection  集合名称
     * @param queryVector 查询向量
     * @param k           返回数量
     * @param modelType   模型类型
     * @param threshold   相似度阈值
     * @return ID与相似度分数的映射
     */
    Map<Long, Double> searchTopK(String index, float[] vector, int k);
    
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