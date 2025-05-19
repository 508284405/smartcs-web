package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.domain.knowledge.gateway.TextSearchGateway;
import com.leyue.smartcs.knowledge.gateway.impl.common.SearchResultDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.redisson.api.search.query.*;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.ListMultiDecoder2;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.redisson.client.protocol.decoder.ObjectMapReplayDecoder;
import org.redisson.client.protocol.decoder.SearchResultDecoderV2;
import org.redisson.codec.CompositeCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public Map<Long, Double> searchByKeyword(String index, String keyword, int k) {
        // 构建查询语句，使用模糊匹配
        String query = "*" + escapeQueryChars(keyword) + "*";
        // 设置查询选项
        QueryOptions options = QueryOptions.defaults()
                .withScores(true)
                .limit(0, k);
        try {
            SearchResult result = searchAsync(index, query, options).join();
            // 处理搜索结果
            Map<Long, Double> resultMap = new HashMap<>();
            for (Document doc : result.getDocuments()) {
                String docId = doc.getId();
                try {
                    double score = doc.getScore();
                    // docId去除索引前缀
                    Long id = Long.parseLong(docId.substring(index.length() + 1));
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

    @Override
    public Map<Long, Double> searchByVectors(String index, byte[] vectors, int k) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("K", k);
        params.put("BLOB", vectors);
        try {
            RFuture<SearchResult> future = searchAsync(index,
                    "*=>[KNN $K @embedding $BLOB AS distance]",
                    QueryOptions.defaults().params(params).withScores(true).dialect(2)
            );
            SearchResult result = future.get();
            // 处理搜索结果
            Map<Long, Double> resultMap = new HashMap<>();
            for (Document doc : result.getDocuments()) {
                String docId = doc.getId();
                try {
                    double score = doc.getScore();
                    // docId去除索引前缀
                    Long id = Long.parseLong(docId.substring(index.length() + 1));
                    resultMap.put(id, score);
                } catch (NumberFormatException e) {
                    log.warn("无法解析文档ID: {}", docId);
                }
            }

            return resultMap;
        } catch (Exception e) {
            log.error("关键词搜索失败: 索引={}, 关键词={}", index, vectors, e);
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
    public boolean indexDocument(String index, Long id, Object source) {
        // 创建索引
        // 将source的key转成FieldIndex
        try {
            String documentKey = index + ":" + id;
            RMap<String, Object> map = redissonClient.getMap(documentKey, new CompositeCodec(StringCodec.INSTANCE, redissonClient.getConfig().getCodec()));

            // 使用RedissonClient存储为哈希 (RediSearch会根据索引配置自动索引)
            map.putAll(JSONObject.parseObject(JSONObject.toJSONString(source)));

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


    public RFuture<SearchResult> searchAsync(String indexName, String query, QueryOptions options) {
        Codec codec = redissonClient.getConfig().getCodec();
        Redisson redisson = (Redisson) redissonClient;

        // 拿到内部的 CommandAsyncExecutor
        CommandAsyncExecutor commandExecutor = redisson.getCommandExecutor();
        List<Object> args = new ArrayList<>();
        args.add(indexName);
        args.add(query);

        if (options.isNoContent()) {
            args.add("NOCONTENT");
        }
        if (options.isVerbatim()) {
            args.add("VERBATIM");
        }
        if (options.isNoStopwords()) {
            args.add("NOSTOPWORDS");
        }
        if (options.isWithScores()) {
            args.add("WITHSCORES");
        }
        if (options.isWithSortKeys()) {
            args.add("WITHSORTKEYS");
        }
        for (QueryFilter filter : options.getFilters()) {
            if (filter instanceof NumericFilterParams) {
                NumericFilterParams params = (NumericFilterParams) filter;
                args.add("FILTER");
                args.add(params.getFieldName());
                args.add(value(params.getMin(), params.isMinExclusive()));
                args.add(value(params.getMax(), params.isMaxExclusive()));
            }
        }
        for (QueryFilter filter : options.getFilters()) {
            if (filter instanceof GeoFilterParams) {
                GeoFilterParams params = (GeoFilterParams) filter;
                args.add("GEOFILTER");
                args.add(params.getFieldName());
                args.add(params.getLongitude());
                args.add(params.getLatitude());
                args.add(params.getRadius());
                args.add(params.getUnit());
            }
        }
        if (!options.getInKeys().isEmpty()) {
            args.add("INKEYS");
            args.add(options.getInKeys().size());
            args.addAll(options.getInKeys());
        }
        if (!options.getInFields().isEmpty()) {
            args.add("INFIELDS");
            args.add(options.getInFields().size());
            args.addAll(options.getInFields());
        }
        if (!options.getReturnAttributes().isEmpty()) {
            args.add("RETURN");
            args.add(options.getReturnAttributes().size());
            int pos = args.size() - 1;
            int amount = 0;
            for (ReturnAttribute attr : options.getReturnAttributes()) {
                args.add(attr.getIdentifier());
                amount++;
                if (attr.getProperty() != null) {
                    args.add("AS");
                    args.add(attr.getProperty());
                    amount += 2;
                }
            }
            args.set(pos, amount);
        }
        if (options.getSummarize() != null) {
            args.add("SUMMARIZE");
            if (!options.getSummarize().getFields().isEmpty()) {
                args.add("FIELDS");
                args.add(options.getSummarize().getFields().size());
                args.addAll(options.getSummarize().getFields());
            }
            if (options.getSummarize().getFragsNum() != null) {
                args.add("FRAGS");
                args.add(options.getSummarize().getFragsNum());
            }
            if (options.getSummarize().getFragSize() != null) {
                args.add("LEN");
                args.add(options.getSummarize().getFragSize());
            }
            if (options.getSummarize().getSeparator() != null) {
                args.add("SEPARATOR");
                args.add(options.getSummarize().getSeparator());
            }
        }
        if (options.getHighlight() != null) {
            args.add("HIGHLIGHT");
            if (!options.getHighlight().getFields().isEmpty()) {
                args.add("FIELDS");
                args.add(options.getHighlight().getFields().size());
                args.addAll(options.getHighlight().getFields());
            }
            if (options.getHighlight().getOpenTag() != null
                    && options.getHighlight().getCloseTag() != null) {
                args.add("TAGS");
                args.add(options.getHighlight().getOpenTag());
                args.add(options.getHighlight().getCloseTag());
            }
        }
        if (options.getSlop() != null) {
            args.add("SLOP");
            args.add(options.getSlop());
        }
        if (options.getTimeout() != null) {
            args.add("TIMEOUT");
            args.add(options.getTimeout());
        }
        if (options.isInOrder()) {
            args.add("INORDER");
        }
        if (options.getLanguage() != null) {
            args.add("LANGUAGE");
            args.add(options.getLanguage());
        }
        if (options.getExpander() != null) {
            args.add("EXPANDER");
            args.add(options.getExpander());
        }
        if (options.getScorer() != null) {
            args.add("SCORER");
            args.add(options.getScorer());
        }
        if (options.isExplainScore()) {
            args.add("EXPLAINSCORE");
        }
        if (options.getSortBy() != null) {
            args.add("SORTBY");
            args.add(options.getSortBy());
            if (options.getSortOrder() != null) {
                args.add(options.getSortOrder());
            }
            if (options.isWithCount()) {
                args.add("WITHCOUNT");
            }
        }
        if (options.getOffset() != null
                && options.getCount() != null) {
            args.add("LIMIT");
            args.add(options.getOffset());
            args.add(options.getCount());
        }
        if (!options.getParams().isEmpty()) {
            if (options.getDialect() == null || options.getDialect() < 2) {
                throw new IllegalArgumentException("When use 'PARAMS', you should set DIALECT to 2 or greater than 2.");
            }
            args.add("PARAMS");
            args.add(options.getParams().size() * 2);
            for (Map.Entry<String, Object> entry : options.getParams().entrySet()) {
                args.add(entry.getKey());
                args.add(entry.getValue());
            }
        }
        if (options.getDialect() != null) {
            args.add("DIALECT");
            args.add(options.getDialect());
        }

        RedisStrictCommand<SearchResult> command;
        if (commandExecutor.getServiceManager().isResp3()) {
            command = new RedisStrictCommand<>("FT.SEARCH",
                    new ListMultiDecoder2(new SearchResultDecoderV2(),
                            new ObjectListReplayDecoder<>(),
                            new ObjectMapReplayDecoder<>(),
                            new ObjectMapReplayDecoder<>(new CompositeCodec(StringCodec.INSTANCE, codec))));
        } else {
            command = new RedisStrictCommand<>("FT.SEARCH",
                    new ListMultiDecoder2(new SearchResultDecoder(options.isWithScores()),
                            new ObjectMapReplayDecoder<>(new CompositeCodec(StringCodec.INSTANCE, codec)),
                            new ObjectListReplayDecoder<>()));
        }

        return commandExecutor.writeAsync(indexName, StringCodec.INSTANCE, command, args.toArray());
    }

    private String value(double score, boolean exclusive) {
        StringBuilder element = new StringBuilder();
        if (Double.isInfinite(score)) {
            if (score > 0) {
                element.append("+inf");
            } else {
                element.append("-inf");
            }
        } else {
            if (exclusive) {
                element.append("(");
            }
            element.append(BigDecimal.valueOf(score).toPlainString());
        }
        return element.toString();
    }
} 