package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.knowledge.convertor.EmbeddingConvertor;
import com.leyue.smartcs.knowledge.dataobject.EmbeddingDO;
import com.leyue.smartcs.knowledge.mapper.EmbeddingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 向量网关实现类
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingGatewayImpl implements EmbeddingGateway {

    private final EmbeddingMapper embeddingMapper;
    private final EmbeddingConvertor embeddingConvertor;
    private final SearchGateway searchGateway;

    @Override
    public Embedding save(Embedding embedding) {
        EmbeddingDO embeddingDO = embeddingConvertor.toDataObject(embedding);

        if (embeddingDO.getId() == null) {
            // 新增
            embeddingMapper.insert(embeddingDO);
            embedding.setId(embeddingDO.getId());
        } else {
            // 更新
            embeddingMapper.updateById(embeddingDO);
        }

        return embeddingConvertor.toDomain(embeddingDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Embedding> saveBatch(List<Embedding> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return new ArrayList<>();
        }

        List<Embedding> result = new ArrayList<>(embeddings.size());

        // 批量保存到MySQL，获取自增ID
        for (Embedding embedding : embeddings) {
            // 保存到数据库
            result.add(save(embedding));
        }

        // 批量写入向量数据到RedisSearch
        try {
            List<Embedding> validEmbeddings = result.stream()
                    .filter(Embedding::isValidVector)
                    .toList();

            if (!validEmbeddings.isEmpty()) {
                // 批量写入向量数据到RedisSearch
                boolean success = searchGateway.batchInsert(Constants.UMBEDDING_INDEX_REDISEARCH, embeddings);
                if (!success) {
                    log.error("批量写入向量数据到RedisSearch失败，Embedding数量: {}", validEmbeddings.size());
                }
            }
        } catch (Exception e) {
            log.error("写入向量数据到RedisSearch时发生错误: {}", e.getMessage(), e);
            // 考虑是否需要回滚MySQL事务
        }

        return result;
    }

    /**
     * 根据模型类型获取向量维度
     */
    private int getVectorDimension(String modelType) {
        // 根据不同的模型类型返回对应的向量维度
        return switch (modelType) {
            case "text-embedding-ada-002" -> 1536; // OpenAI Ada-002模型
            case "text-embedding-3-small" -> 1536; // OpenAI text-embedding-3-small模型
            case "text-embedding-3-large" -> 3072; // OpenAI text-embedding-3-large模型
            default -> 1536; // 默认维度
        };
    }

    @Override
    public Optional<Embedding> findById(Long id) {
        EmbeddingDO embeddingDO = embeddingMapper.selectById(id);
        return Optional.ofNullable(embeddingDO).map(embeddingConvertor::toDomain);
    }

    @Override
    public List<Embedding> findByDocId(Long docId) {
        List<EmbeddingDO> embeddingDOs = embeddingMapper.findByDocId(docId);
        return embeddingConvertor.toDomainList(embeddingDOs);
    }

    @Override
    public Optional<Embedding> findByDocIdAndSectionIdx(Long docId, Integer sectionIdx) {
        EmbeddingDO embeddingDO = embeddingMapper.findByDocIdAndSectionIdx(docId, sectionIdx);
        return Optional.ofNullable(embeddingDO).map(embeddingConvertor::toDomain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByDocId(Long docId) {
        // 获取要删除的Embedding ID列表
        List<EmbeddingDO> embeddingDOs = embeddingMapper.findByDocId(docId);
        List<Long> embeddingIds = embeddingDOs.stream()
                .map(EmbeddingDO::getId)
                .collect(Collectors.toList());

        // 从MySQL删除
        int rows = embeddingMapper.deleteByDocId(docId);

        // 从RedisSearch删除向量数据
        if (!embeddingIds.isEmpty()) {
            try {
//                boolean success = searchGateway.delete(Constants.UMBEDDING_INDEX_REDISEARCH, embeddingIds);
//                if (!success) {
//                    log.error("从RedisSearch删除向量数据失败，文档ID: {}, Embedding数量: {}", docId, embeddingIds.size());
//                }
            } catch (Exception e) {
                log.error("从RedisSearch删除向量数据时发生错误: {}", e.getMessage(), e);
                // 考虑是否需要回滚MySQL事务
            }
        }

        return rows > 0;
    }
}