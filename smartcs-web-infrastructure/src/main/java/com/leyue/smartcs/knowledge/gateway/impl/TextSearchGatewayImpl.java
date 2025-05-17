package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.gateway.TextSearchGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.redisson.api.search.query.Document;
import org.redisson.api.search.query.QueryOptions;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 全文检索网关实现类，基于RediSearch实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TextSearchGatewayImpl implements TextSearchGateway {

    private final RedissonClient redissonClient;

    /**
     * 关键词搜索
     *
     * @param index   索引名称
     * @param keyword 关键词
     * @param k       返回数量
     * @return ID与分数的映射
     */
    @Override
    public Map<Long, Float> searchByKeyword(String index, String keyword, int k) {
        try {
            RSearch search = redissonClient.getSearch();

            // 构建查询语句，使用模糊匹配
            String query = "*" + keyword + "*";

            // 设置查询选项
            QueryOptions options = QueryOptions.defaults()
                    .withScores(true)
                    .limit(0, k);

            // 执行搜索
            SearchResult result = search.search(index, query, options);

            // 处理搜索结果
            Map<Long, Float> resultMap = new HashMap<>();
            for (Document doc : result.getDocuments()) {
                String docId = doc.getId();
                try {
                    float score = doc.getScore().floatValue();
                    // docId去除索引前缀
                    Long id = Long.parseLong(docId.substring(index.indexOf(":") + 1));
                    resultMap.put(id, score);
                } catch (NumberFormatException e) {
                    log.warn("无法解析文档ID: {}", docId);
                }
            }

            return resultMap;
        } catch (Exception e) {
            log.error("关键词搜索失败: 索引={}, 关键词={}", index, keyword, e);
            return new HashMap<>();
        }
    }

    /**
     * 模糊搜索
     *
     * @param index 索引名称
     * @param text  模糊文本
     * @param field 搜索字段
     * @param k     返回数量
     * @return ID与分数的映射
     */
    @Override
    public Map<Long, Float> searchFuzzy(String index, String text, String field, int k) {
        try {
            RSearch search = redissonClient.getSearch();

            // 构建模糊查询语句
            String query = String.format("@%s:*%s*", field, escapeQueryChars(text));

            // 设置查询选项
            QueryOptions options = QueryOptions.defaults()
                    .limit(0, k);

            // 执行搜索
            SearchResult result = search.search(index, query, options);

            // 处理搜索结果
            Map<Long, Float> resultMap = new HashMap<>();
            for (Document doc : result.getDocuments()) {
                try {
                    Long id = Long.valueOf(doc.getId());
                    float score = doc.getScore().floatValue();
                    resultMap.put(id, score);
                } catch (NumberFormatException e) {
                    log.warn("无法解析文档ID: {}", doc.getId());
                }
            }

            return resultMap;
        } catch (Exception e) {
            log.error("模糊搜索失败: 索引={}, 文本={}, 字段={}", index, text, field, e);
            return new HashMap<>();
        }
    }

    /**
     * 创建或更新索引文档
     *
     * @param index  索引名称
     * @param id     文档ID
     * @param source 文档内容
     * @return 是否成功
     */
    @Override
    public boolean indexDocument(String index, Long id, Map<String, Object> source) {
        // 创建索引
        // 将source的key转成FieldIndex
        try {
            String documentKey = index + ":" + id;
            RMap<String, Object> map = redissonClient.getMap(documentKey);

            // 使用RedissonClient存储为哈希 (RediSearch会根据索引配置自动索引)
            map.putAll(source);

            log.info("成功索引文档: index={}, id={}", index, id);
            return true;
        } catch (Exception e) {
            log.error("索引文档失败: index={}, id={}", index, id, e);
            return false;
        }
    }

    /**
     * 删除索引文档
     *
     * @param index 索引名称
     * @param id    文档ID
     * @return 是否成功
     */
    @Override
    public boolean deleteDocument(String index, Long id) {
        try {
            // 使用RedissonClient删除哈希
            String documentKey = index + ":" + id;
            redissonClient.getMap(documentKey).delete();

            log.info("成功删除文档: index={}, id={}", index, id);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败: index={}, id={}", index, id, e);
            return false;
        }
    }

    /**
     * 创建全文索引 - 扩展方法
     *
     * @param index 索引名称
     * @return 是否成功
     */
    public boolean createIndex(String index, FieldIndex... fieldIndex) {
        try {
            RSearch search = redissonClient.getSearch();


            // 创建索引配置
            IndexOptions options = IndexOptions.defaults()
                    .on(IndexType.HASH)
                    .prefix(index + ":");

            log.info("尝试创建全文索引: {}", index);

            // 调用RediSearch API创建索引
            search.createIndex(index, options, fieldIndex);

            log.info("成功创建全文索引: {}", index);
            return true;
        } catch (Exception e) {
            // 检查是否是索引已存在的异常，如果是则视为成功
            if (e.getMessage() != null && e.getMessage().contains("Index already exists")) {
                log.warn("全文索引已存在，无需重复创建: {}", index);
                return true;
            }
            log.error("创建全文索引失败: {}", index, e);
            return false;
        }
    }

    /**
     * 删除索引 - 扩展方法
     *
     * @param index 索引名称
     * @return 是否成功
     */
    public boolean deleteIndex(String index) {
        try {
            RSearch search = redissonClient.getSearch();

            // 删除索引
            search.dropIndex(index);

            log.info("成功删除索引: {}", index);
            return true;
        } catch (Exception e) {
            log.error("删除索引失败: {}", index, e);
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
} 