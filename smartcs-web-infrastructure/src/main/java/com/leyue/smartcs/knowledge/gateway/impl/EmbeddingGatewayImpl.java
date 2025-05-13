package com.leyue.smartcs.knowledge.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
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

/**
 * 向量网关实现类
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingGatewayImpl implements EmbeddingGateway {
    
    private final EmbeddingMapper embeddingMapper;
    private final EmbeddingConvertor embeddingConvertor;
    
    @Override
    public Embedding save(Embedding embedding) {
        EmbeddingDO embeddingDO = embeddingConvertor.toDataObject(embedding);
        
        if (embeddingDO.getId() == null) {
            // 新增
            embeddingMapper.insert(embeddingDO);
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
        for (Embedding embedding : embeddings) {
            result.add(save(embedding));
        }
        
        return result;
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
    public boolean deleteByDocId(Long docId) {
        int rows = embeddingMapper.deleteByDocId(docId);
        return rows > 0;
    }
}