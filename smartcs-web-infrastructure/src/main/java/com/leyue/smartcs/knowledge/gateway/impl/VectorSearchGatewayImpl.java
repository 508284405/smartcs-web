package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.gateway.VectorSearchGateway;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.query.Document;
import org.redisson.api.search.query.QueryOptions;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量检索网关实现类
 * 使用RediSearch的向量检索API实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class VectorSearchGatewayImpl implements VectorSearchGateway {

    private final RedissonClient redissonClient;

    // 向量数据缓存的过期时间（30天）
    private static final long VECTOR_CACHE_TTL = 30;

    @Override
    public boolean batchInsert(String index, List<Embedding> embeddings) {
        try {
            // 批量写入
            for (Embedding embedding : embeddings) {
                Long id = embedding.getId();
                float[] vector = embedding.getVector();
                // 构建Redis键名（格式: index:vector:id）
                String vectorKey = index + id;
                // 添加向量数据（使用ByteArrayCodec）
                RMap<String, byte[]> rmap = redissonClient.getMap(vectorKey, new CompositeCodec(StringCodec.INSTANCE, ByteArrayCodec.INSTANCE));
                rmap.put("embedding", JedisTextSearchGatewayImpl.floatsToBytes(vector));
                rmap.expire(Duration.ofDays(VECTOR_CACHE_TTL));
            }
            return true;
        } catch (Exception e) {
            log.error("向量批量写入失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 在指定集合中搜索与查询向量最相似的Top-K个向量。
     */
    @Override
    public Map<Long, Double> searchTopK(String index, float[] queryVector, int k) {
        // 尝试使用RediSearch的向量搜索功能
        String query = "*=>[KNN " + k + " @vector $vector AS score]";

        Map<String, Object> params = new HashMap<>();
        params.put("vector", JedisTextSearchGatewayImpl.floatsToBytes(queryVector));
        SearchResult result = redissonClient.getSearch().search(index,
                query,
                QueryOptions.defaults()
                        .params(params)
                        .dialect(2));

        Map<Long, Double> similarities = new HashMap<>();

        for (Document document : result.getDocuments()) {
            // 从文档键中提取ID
            String key = document.getId();
            Long id = Long.parseLong(key.substring(index.length()));
            Double score = document.getScore();
            similarities.put(id, score);
        }
        return similarities;
    }
} 