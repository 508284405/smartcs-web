package com.leyue.smartcs.knowledge.gateway.impl;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.domain.utils.RedisearchUtils;
import com.leyue.smartcs.dto.knowledge.EmbeddingCmd;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexInfo;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于Jedis的搜索网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class JedisSearchGatewayImpl implements SearchGateway {

    private final UnifiedJedis unifiedJedis;
    private final RedissonClient redissonClient;
    // 初始化分词器
    private final HuggingFaceTokenizer sentenceTokenizer = HuggingFaceTokenizer.newInstance(
            "sentence-transformers/all-mpnet-base-v2",
            Map.of("maxLength", "1536", "modelMaxLength", "1536")
    );

    // ========== 向量检索相关方法 ==========

    @Override
    public boolean batchEmbeddingInsert(String index, List<EmbeddingCmd> embeddings) {
        try {
            // 批量写入
            for (EmbeddingCmd embedding : embeddings) {
                Long id = embedding.getId();
                String text = embedding.getText();
                // 构建Redis键名（格式: collection:id）
                String vectorKey = index + ":" + id;
                // 添加向量数据（使用ByteArrayCodec）
                unifiedJedis.hset(vectorKey.getBytes(), "embedding".getBytes(), RedisearchUtils.longsToFloatsByteString(sentenceTokenizer.encode(text).getIds()));
            }
            return true;
        } catch (Exception e) {
            log.error("向量批量写入失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<Long, Double> searchTopK(String index, String keyword, int k) {
        // Query for KNN, assuming 'embedding' is the vector field name.
        String queryString = "*=>[KNN $K @embedding $BLOB AS distance]";
        Query query = new Query(queryString)
                .returnFields("embedding", "distance", "score")
                .addParam("K", String.valueOf(k))
                .addParam("BLOB", RedisearchUtils.longsToFloatsByteString(sentenceTokenizer.encode(keyword).getIds()))
                .setSortBy("distance", true)
                .dialect(2);

        Map<Long, Double> resultMap = new HashMap<>();
        try {
            SearchResult searchResult = unifiedJedis.ftSearch(index, query);
            if (searchResult.getTotalResults() > 0) {
                for (Document doc : searchResult.getDocuments()) {
                    try {
                        String docId = doc.getId();
                        Long id = Long.parseLong(docId.substring(index.length() + 1));
                        // The score is returned in the 'score' field due to "AS score" score -> 527579744
                        doc.getProperties().forEach(entry -> {
                            if ("distance".equals(entry.getKey())) {
                                resultMap.put(id, Double.parseDouble((String) entry.getValue()));
                            }
                        });
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

    // ========== 全文检索相关方法 ==========

    @Override
    public Map<Long, Double> searchByKeyword(String index, String keyword, int k) {
        // 构建模糊查询语句
        String queryString = "*" + escapeQueryChars(keyword) + "*";
        Query query = new Query(queryString)
                .limit(0, k)
                .returnFields("score");

        Map<Long, Double> resultMap = new HashMap<>();
        try {
            SearchResult searchResult = unifiedJedis.ftSearch(index, query);
            if (searchResult.getTotalResults() > 0) {
                for (Document doc : searchResult.getDocuments()) {
                    try {
                        String docId = doc.getId();
                        Long id = Long.parseLong(docId.substring(index.length() + 1));
                        resultMap.put(id, doc.getScore());
                    } catch (Exception e) {
                        log.warn("解析搜索结果失败 for docId: {}", doc.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("关键词搜索失败: 索引={}, 关键词={}", index, keyword, e);
            return new HashMap<>();
        }
        return resultMap;
    }

    @Override
    public boolean indexDocument(String index, Long id, Object source) {
        try {
            String documentKey = index + ":" + id;
            Map<String, String> documentMap = JSONObject.parseObject(JSONObject.toJSONString(source), new TypeReference<Map<String, String>>() {
            });

            // 使用Jedis存储为哈希 (RediSearch会根据索引配置自动索引)
            unifiedJedis.hset(documentKey, documentMap);

            log.info("成功索引文档: index={}, id={}", index, id);
            return true;
        } catch (Exception e) {
            log.error("索引文档失败: index={}, id={}", index, id, e);
            return false;
        }
    }

    @Override
    public boolean deleteDocument(String index, Long id) {
        try {
            // 使用Jedis删除哈希
            String documentKey = index + ":" + id;
            unifiedJedis.del(documentKey);

            log.info("成功删除文档: index={}, id={}", index, id);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败: index={}, id={}", index, id, e);
            return false;
        }
    }

    /**
     * 转义RediSearch查询中的特殊字符
     *
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String escapeQueryChars(String input) {
        if (input == null) {
            return "";
        }
        // 转义RediSearch查询中的特殊字符
        return input.replaceAll("([,\\\\/\\!\\{\\}\\[\\]\\(\\)\\\"\\^~\\*:])", "\\\\$1");
    }

    // ========== 索引管理相关方法 ==========

    @Override
    public void createIndex(String index, Object... fieldIndex) {
        try {
            RSearch search = redissonClient.getSearch();

            // 创建索引配置
            IndexOptions options = IndexOptions.defaults()
                    .on(IndexType.HASH)
                    .prefix(index + ":");

            log.info("尝试创建全文索引: {}", index);

            // 转换Object数组为FieldIndex数组
            FieldIndex[] fieldIndexArray = new FieldIndex[fieldIndex.length];
            for (int i = 0; i < fieldIndex.length; i++) {
                fieldIndexArray[i] = (FieldIndex) fieldIndex[i];
            }

            // 调用RediSearch API创建索引
            search.createIndex(index, options, fieldIndexArray);

            log.info("成功创建全文索引: {}", index);
        } catch (Exception e) {
            // 检查是否是索引已存在的异常，如果是则视为成功
            if (e.getMessage() != null && e.getMessage().contains("Index already exists")) {
                log.warn("全文索引已存在，无需重复创建: {}", index);
            } else {
                log.error("创建全文索引失败: {}", index, e);
            }
        }
    }

    @Override
    public IndexInfoDTO getIndexInfo(String indexName) {
        // 使用Redisson的info方法获取索引信息
        IndexInfo indexInfo = redissonClient.getSearch().info(indexName);
        if (indexInfo == null) {
            log.error("索引不存在: {}", indexName);
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(indexInfo), IndexInfoDTO.class);
    }

    @Override
    public boolean deleteIndex(String indexName) {
        try {
            RSearch search = redissonClient.getSearch();
            // 删除索引
            search.dropIndex(indexName);
            log.info("成功删除索引: {}", indexName);
            return true;
        } catch (Exception e) {
            log.error("删除索引失败: {}", indexName, e);
            return false;
        }
    }

    @Override
    public List<String> listIndexes() {
        try {
            return redissonClient.getSearch().getIndexes();
        } catch (Exception e) {
            log.error("获取索引列表失败", e);
            return List.of();
        }
    }
} 