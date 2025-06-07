package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于Redisson的搜索网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonSearchGatewayImpl implements SearchGateway {

    private final RedissonClient redissonClient;

    @Override
    public boolean indexDocument(String index, Long id, Object source) {
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
            // 创建索引配置
            org.redisson.api.search.index.IndexOptions options = org.redisson.api.search.index.IndexOptions.defaults()
                    .on(org.redisson.api.search.index.IndexType.HASH)
                    .prefix(index + ":");

            log.info("尝试创建全文索引: {}", index);

            // 转换Object数组为FieldIndex数组
            org.redisson.api.search.index.FieldIndex[] fieldIndexArray = new org.redisson.api.search.index.FieldIndex[fieldIndex.length];
            for (int i = 0; i < fieldIndex.length; i++) {
                fieldIndexArray[i] = (org.redisson.api.search.index.FieldIndex) fieldIndex[i];
            }

            // 调用RediSearch API创建索引
            redissonClient.getSearch().createIndex(index, options, fieldIndexArray);

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
        org.redisson.api.search.index.IndexInfo indexInfo = redissonClient.getSearch().info(indexName);
        if (indexInfo == null) {
            log.error("索引不存在: {}", indexName);
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(indexInfo), IndexInfoDTO.class);
    }

    @Override
    public boolean deleteIndex(String indexName) {
        try {
            // 删除索引
            redissonClient.getSearch().dropIndex(indexName);
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