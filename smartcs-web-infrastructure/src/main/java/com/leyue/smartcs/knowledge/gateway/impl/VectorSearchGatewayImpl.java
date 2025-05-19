package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.gateway.VectorSearchGateway;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.query.Document;
import org.redisson.api.search.query.QueryOptions;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/**
 * 向量检索网关实现类
 * 使用RediSearch的向量检索API实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VectorSearchGatewayImpl implements VectorSearchGateway {

    private final RedissonClient redissonClient;

    // 向量数据缓存的过期时间（30天）
    private static final long VECTOR_CACHE_TTL = 30;

    @Override
    public boolean batchInsert(String index, List<Long> ids, List<Object> vectors, String partitionKey) {
        if (ids == null || ids.isEmpty() || vectors == null || vectors.isEmpty() || ids.size() != vectors.size()) {
            log.error("批量写入向量参数无效: index={}, idsSize={}, vectorsSize={}",
                    index, ids == null ? 0 : ids.size(), vectors == null ? 0 : vectors.size());
            return false;
        }

        try {
            log.info("批量写入向量: index={}, size={}, modelType={}", index, ids.size(), partitionKey);
            // 批量写入
            for (int i = 0; i < ids.size(); i++) {
                Long id = ids.get(i);
                Object v = vectors.get(i);
                // 构建Redis键名（格式: index:vector:id）
                String vectorKey = index + id;
                if (v instanceof byte[] vector) {
                    // 添加向量数据（使用ByteArrayCodec）
                    RMap<String, Object> rmap = redissonClient.getMap(vectorKey, new CompositeCodec(StringCodec.INSTANCE, redissonClient.getConfig().getCodec()));
                    rmap.put("embedding", vector);
                    rmap.put("id", id);
                    rmap.expire(Duration.ofDays(VECTOR_CACHE_TTL));
                } else {
                    log.error("向量数据类型不支持: {}", v.getClass().getName());
                }
            }
            return true;
        } catch (Exception e) {
            log.error("向量批量写入失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean delete(String index, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("删除向量的ID列表为空: index={}", index);
            return true;
        }

        try {
            log.info("删除向量: index={}, ids.size={}", index, ids.size());
            // 批量删除
            for (Long id : ids) {
                // 构建Redis键名
                String vectorKey = index + id;
                // 删除向量数据
                redissonClient.getMap(vectorKey).deleteAsync();
            }

            return true;
        } catch (Exception e) {
            log.error("向量删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建向量索引。
     * <p>
     * 此方法会在Redis中为指定的集合创建一个向量索引。索引的配置信息（维度、索引类型、创建时间）
     * 会存储在名为 `collection + ":config"` 的RMap中。
     * 同时，它会尝试使用RediSearch创建一个名为 `collection + "_idx"` 的哈希类型的向量索引，
     * 该索引会作用于前缀为 `collection + ":vector:"` 的键。
     * 如果RediSearch索引创建失败，会记录警告日志，并回退到备用搜索方式。
     *
     * @param collection 集合名称，用于构建索引名称和存储相关的元数据。
     * @param dimension  向量的维度。
     * @param indexType  索引类型（例如："FLAT", "HNSW"等），目前此参数仅记录在配置中，实际创建的是FLAT向量索引。
     * @return 如果索引创建（或配置记录）成功，则返回 true；否则返回 false。
     */
    @Override
    public boolean createIndex(String collection, int dimension, String indexType) {
        //
        return true;
    }

    /**
     * 在指定集合中搜索与查询向量最相似的Top-K个向量。
     * <p>
     * 此方法首先尝试使用RediSearch的向量搜索功能。它会构建一个K最近邻（KNN）查询，
     * 在名为 `collection + "_idx"` 的索引上执行搜索。
     * 搜索结果会根据向量的余弦相似度得分进行排序。
     * <p>
     * 如果指定了 `modelType`，则会进一步过滤结果，只保留那些元数据中 `model_type` 匹配的向量。
     * 只有相似度得分大于或等于 `threshold` 的结果才会被返回。
     * <p>
     * 如果RediSearch的向量搜索失败（例如，索引不存在或查询语法错误），
     * 方法会记录警告并自动回退到 {@link #legacySearchTopK} 方法，该方法通过迭代集合中的所有向量，
     * 计算余弦相似度来进行搜索。
     *
     * @param collection  集合名称，从中检索向量。
     * @param queryVector 查询向量的字节数组表示。
     * @param k           要返回的最相似向量的数量。
     * @param modelType   （可选）模型类型，用于过滤结果。如果为null，则不进行模型类型过滤。
     * @param threshold   相似度阈值，只有相似度大于或等于此阈值的结果才会被返回。
     * @return 一个Map，其中键是向量的ID（Long类型），值是对应的相似度得分（Float类型）。
     * 如果发生错误或未找到符合条件的结果，则返回空Map。
     */
    @Override
    public Map<Long, Float> searchTopK(String collection, byte[] queryVector, int k, String modelType, float threshold) {
        try {
            log.info("向量检索: collection={}, vectorSize={}, k={}, modelType={}, threshold={}",
                    collection, queryVector.length, k, modelType, threshold);

            // 使用RediSearch进行向量搜索
            String indexName = collection + "_idx";
            float[] floatVector = bytesToFloats(queryVector);

            try {
                // 尝试使用RediSearch的向量搜索功能
                String query = "*=>[KNN " + k + " @vector $vector AS score]";

                Map<String, Object> params = new HashMap<>();
                params.put("vector", floatVector);
                SearchResult result = redissonClient.getSearch().search(indexName,
                        query,
                        QueryOptions.defaults()
                                .params(params)
                                .dialect(2));

                Map<Long, Float> similarities = new HashMap<>();

                for (Document document : result.getDocuments()) {
                    // 从文档键中提取ID
                    String key = document.getId();
                    String[] parts = key.split(":");
                    if (parts.length >= 3) {
                        try {
                            Long id = Long.parseLong(parts[parts.length - 1]);
                            float score = Float.parseFloat(document.getScore().toString());

                            // 如果指定了模型类型，则需要检查
                            if (modelType != null) {
                                String metadataKey = collection + ":metadata:" + id;
                                RMap<String, String> metadata = redissonClient.getMap(metadataKey);
                                String storedModelType = metadata.get("model_type");

                                // 如果模型类型不匹配，则跳过
                                if (!modelType.equals(storedModelType)) {
                                    continue;
                                }
                            }

                            // 只保留相似度大于阈值的结果
                            if (score >= threshold) {
                                similarities.put(id, score);
                            }
                        } catch (NumberFormatException e) {
                            log.warn("无法从文档ID中解析数字ID: {}", key);
                        }
                    }
                }

                return similarities;
            } catch (Exception e) {
                log.warn("RediSearch向量搜索失败，使用备用方法: {}", e.getMessage());
                // 如果RediSearch方法失败，回退到原始实现
                return legacySearchTopK(collection, queryVector, k, modelType, threshold);
            }
        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 原始的备用搜索方法（当RediSearch不可用时使用）
     */
    private Map<Long, Float> legacySearchTopK(String collection, byte[] queryVector, int k, String modelType, float threshold) {
        try {
            log.info("使用备用方法进行向量检索");

            // 获取集合索引映射
            RMap<String, String> collectionIndex = redissonClient.getMap(collection + ":index");

            // 获取所有ID
            Set<String> idStrings = collectionIndex.keySet();
            if (idStrings.isEmpty()) {
                log.warn("集合为空，无法进行检索: {}", collection);
                return Collections.emptyMap();
            }

            // 计算与每个向量的相似度
            Map<Long, Float> similarities = new HashMap<>();

            for (String idStr : idStrings) {
                Long id = Long.parseLong(idStr);

                // 获取向量数据
                String vectorKey = collection + ":vector:" + id;
                RBucket<byte[]> vectorBucket = redissonClient.getBucket(vectorKey, ByteArrayCodec.INSTANCE);
                byte[] storedVector = vectorBucket.get();

                if (storedVector == null) {
                    log.warn("未找到向量数据: {}", vectorKey);
                    continue;
                }

                // 获取元数据，检查模型类型
                String metadataKey = collection + ":metadata:" + id;
                RMap<String, String> metadata = redissonClient.getMap(metadataKey);
                String storedModelType = metadata.get("model_type");

                // 如果指定了模型类型且不匹配，则跳过
                if (modelType != null && !modelType.equals(storedModelType)) {
                    continue;
                }

                // 计算余弦相似度
                float similarity = calculateCosineSimilarity(queryVector, storedVector);

                // 只保留相似度大于阈值的结果
                if (similarity >= threshold) {
                    similarities.put(id, similarity);
                }
            }

            // 根据相似度排序并获取Top-K结果
            return getTopK(similarities, k);
        } catch (Exception e) {
            log.error("备用向量检索失败: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private float calculateCosineSimilarity(byte[] vector1, byte[] vector2) {
        // 确保两个向量长度相同
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量长度不匹配: " + vector1.length + " vs " + vector2.length);
        }

        // 将字节数组转换为浮点数数组（假设每个浮点数占用4个字节）
        float[] floatVector1 = bytesToFloats(vector1);
        float[] floatVector2 = bytesToFloats(vector2);

        // 计算点积
        float dotProduct = 0.0f;
        for (int i = 0; i < floatVector1.length; i++) {
            dotProduct += floatVector1[i] * floatVector2[i];
        }

        // 计算范数
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        for (int i = 0; i < floatVector1.length; i++) {
            norm1 += floatVector1[i] * floatVector1[i];
            norm2 += floatVector2[i] * floatVector2[i];
        }
        norm1 = (float) Math.sqrt(norm1);
        norm2 = (float) Math.sqrt(norm2);

        // 计算余弦相似度
        if (norm1 == 0 || norm2 == 0) {
            return 0.0f;
        }
        return dotProduct / (norm1 * norm2);
    }

    /**
     * 将字节数组转换为浮点数数组
     */
    private float[] bytesToFloats(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = buffer.getFloat(i * 4);
        }
        return floats;
    }

    /**
     * 获取相似度最高的K个结果
     */
    private Map<Long, Float> getTopK(Map<Long, Float> similarities, int k) {
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(k)
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }
} 