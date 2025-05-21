package com.leyue.smartcs.knowledge.mapper;

import com.alibaba.cola.exception.BizException;
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

import java.util.List;

/**
 * Redisearch 操作实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RediSearchMapper {

    private final RedissonClient redissonClient;
    private final EmbeddingConvertor embeddingConvertor;

    public void createIndex(String index, FieldIndex... fieldIndex) {
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
        } catch (Exception e) {
            // 检查是否是索引已存在的异常，如果是则视为成功
            if (e.getMessage() != null && e.getMessage().contains("Index already exists")) {
                log.warn("全文索引已存在，无需重复创建: {}", index);
            }
            log.error("创建全文索引失败: {}", index, e);
        }
    }

    public IndexInfoDTO getIndexInfo(String indexName) {
        // 使用Redisson的info方法获取索引信息
        IndexInfo indexInfo = redissonClient.getSearch().info(indexName);
        if (indexInfo == null) {
            throw new BizException("NOT_FOUND", "索引不存在: " + indexName);
        }
        return embeddingConvertor.toIndexInfoDTO(indexInfo);
    }

    public boolean deleteIndex(String indexName) {
        RSearch search = redissonClient.getSearch();
        // 删除索引
        search.dropIndex(indexName);
        return true;
    }

    public List<String> listIndexes() {
        return redissonClient.getSearch().getIndexes();
    }
} 