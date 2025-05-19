package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.RedisearchGateway;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;
import com.leyue.smartcs.knowledge.convertor.EmbeddingConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexInfo;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Redisearch 操作实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisearchGatewayImpl implements RedisearchGateway {

    private final RedissonClient redissonClient;
    private final EmbeddingConvertor embeddingConvertor;

    @Override
    public void createIndex(String indexName, String prefix, Map<String, String> schema, boolean replaceIfExists) {
        RSearch search = redissonClient.getSearch();

        // 如果需要替换现有索引
        if (replaceIfExists) {
            try {
                search.dropIndex(indexName);
                log.info("已删除现有索引: {}", indexName);
            } catch (Exception e) {
                log.warn("删除现有索引时出现错误，可能不存在: {}", indexName);
            }
        }

        try {
            // 创建索引选项
            IndexOptions options = IndexOptions.defaults();

            // 设置前缀，如果存在
            if (prefix != null && !prefix.trim().isEmpty()) {
                options.prefix(prefix);
            }

            // 索引类型，根据实际业务调整，这里假设为HASH
            options.on(IndexType.HASH); // Or IndexType.JSON

            // 转换字段定义为FieldIndex
            List<FieldIndex> fields = new ArrayList<>();
            for (Map.Entry<String, String> entry : schema.entrySet()) {
                String fieldName = entry.getKey();
                String fieldType = entry.getValue();

                // 根据类型字符串创建对应的FieldIndex
                FieldIndex fieldIndex;
                switch (fieldType.toUpperCase()) {
                    case "TEXT":
                        fieldIndex = FieldIndex.text(fieldName);
                        break;
                    case "TAG":
                        fieldIndex = FieldIndex.tag(fieldName);
                        break;
                    case "NUMERIC":
                        fieldIndex = FieldIndex.numeric(fieldName);
                        break;
                    case "GEO":
                        fieldIndex = FieldIndex.geo(fieldName);
                        break;
                    default:
                        fieldIndex = FieldIndex.text(fieldName); // 默认为TEXT
                }

                fields.add(fieldIndex);
            }

            // 创建索引
            search.createIndex(indexName, options, fields.toArray(new FieldIndex[0]));
            log.info("索引创建成功: {}", indexName);
        } catch (Exception e) {
            log.error("创建索引失败: " + indexName, e);
            throw new RuntimeException("创建索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IndexInfoDTO getIndexInfo(String indexName) {
        // 使用Redisson的info方法获取索引信息
        IndexInfo indexInfo = redissonClient.getSearch().info(indexName);
        if (indexInfo == null) {
            throw new BizException("NOT_FOUND", "索引不存在: " + indexName);
        }
        return embeddingConvertor.toIndexInfoDTO(indexInfo);
    }

    @Override
    public boolean deleteIndex(String indexName) {
        RSearch search = redissonClient.getSearch();
        // 删除索引
        search.dropIndex(indexName);
        return true;
    }

    @Override
    public List<String> listIndexes() {
        return redissonClient.getSearch().getIndexes();
    }
} 