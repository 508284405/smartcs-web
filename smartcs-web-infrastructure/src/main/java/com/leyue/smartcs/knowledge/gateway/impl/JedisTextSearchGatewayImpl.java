package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.gateway.VectorSearchGateway;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全文检索网关实现类，基于Jedis实现RediSearch
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JedisTextSearchGatewayImpl implements VectorSearchGateway {

    private final UnifiedJedis unifiedJedis;

    /**
     * 向量搜索
     *
     * @param index  索引名称
     * @param vector 向量数据 (byte array for FLOAT32)
     * @param k      返回数量
     * @return ID与分数的映射 (distance as score)
     */
    @Override
    public Map<Long, Double> searchTopK(String index, float[] vector, int k) {
        // Query for KNN, assuming 'embedding' is the vector field name.
        String queryString = "(*)=>[KNN $K @embedding $BLOB AS score]";
        Query query = new Query(queryString)
                .addParam("K", String.valueOf(k))
                .addParam("BLOB", floatsToBytes(vector))
                .returnFields("score")
                .dialect(2);

        Map<Long, Double> resultMap = new HashMap<>();
        try {
            SearchResult searchResult = unifiedJedis.ftSearch(index, query);
            if (searchResult.getTotalResults() > 0) {
                for (Document doc : searchResult.getDocuments()) {
                    try {
                        String docId = doc.getId();
                        Long id = Long.parseLong(docId.substring(index.length() + 1));
                        // The score is returned in the 'score' field due to "AS score"
                        resultMap.put(id, Double.parseDouble(doc.getString("score")));
                    } catch (Exception e) {
                        log.warn("解析向量搜索结果失败 for docId: {}", doc.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("向量搜索失败: 索引={}", index, e);
            return new HashMap<>();
        }
        return resultMap;
    }

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
                unifiedJedis.hset(vectorKey.getBytes(), "embedding".getBytes(), floatsToBytes(vector));
            }
            return true;
        } catch (Exception e) {
            log.error("向量批量写入失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public static byte[] longsToFloatsByteString(long[] input) {
        float[] floats = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            floats[i] = input[i];
        }

        byte[] bytes = new byte[Float.BYTES * floats.length];
        ByteBuffer
                .wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asFloatBuffer()
                .put(floats);
        return bytes;
    }

    public static byte[] floatsToBytes(float[] input) {
        byte[] bytes = new byte[Float.BYTES * input.length];
        ByteBuffer
                .wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asFloatBuffer()
                .put(input);
        return bytes;
    }
} 