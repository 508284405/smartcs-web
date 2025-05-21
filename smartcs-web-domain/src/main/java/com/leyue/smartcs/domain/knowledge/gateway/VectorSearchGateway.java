package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.model.Embedding;

import java.util.List;
import java.util.Map;

/**
 * 向量检索网关接口
 */
public interface VectorSearchGateway {
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
} 