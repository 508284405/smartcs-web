package com.leyue.smartcs.knowledge.gateway.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.domain.knowledge.Embedding;
import com.leyue.smartcs.dto.knowledge.EmbeddingCmd;
import com.leyue.smartcs.dto.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.knowledge.convertor.EmbeddingConvertor;
import com.leyue.smartcs.knowledge.dataobject.EmbeddingDO;
import com.leyue.smartcs.knowledge.mapper.EmbeddingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public List<Embedding> saveBatch(Long docId, List<String> contentChunks, StrategyNameEnum strategyName) {
        if (contentChunks == null || contentChunks.isEmpty()) {
            return new ArrayList<>();
        }

        List<Embedding> result = new ArrayList<>(contentChunks.size());

        // 批量保存到MySQL，获取自增ID
        for (String content : contentChunks) {
            // 保存到数据库
            Embedding embedding = new Embedding();
            embedding.setDocId(docId);
            embedding.setSectionIdx(contentChunks.indexOf(content));
            embedding.setContentSnip(content);
            embedding.setStrategyName(strategyName);
            result.add(save(embedding));
        }

        // 批量写入向量数据到RedisSearch
        try {
            // 批量写入向量数据到RedisSearch
            List<EmbeddingCmd> embeddingCmdList = result.stream().map(x -> {
                EmbeddingCmd cmd = new EmbeddingCmd();
                cmd.setId(x.getId());
                cmd.setText(x.getContentSnip());
                return cmd;
            }).collect(Collectors.toList());
            boolean success = searchGateway.batchEmbeddingInsert(Constants.UMBEDDING_INDEX_REDISEARCH, embeddingCmdList);
            if (!success) {
                log.error("批量写入向量数据到RedisSearch失败，Embedding数量: {}", result.size());
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
    public boolean deleteByDocId(Long docId, StrategyNameEnum strategyName) {
        // 从MySQL删除
        int rows = embeddingMapper.delete(Wrappers.<EmbeddingDO>lambdaQuery()
                .eq(EmbeddingDO::getDocId, docId)
                .eq(EmbeddingDO::getStrategyName, strategyName)
        );
        return rows > 0;
    }
}