package com.leyue.smartcs.knowledge.gateway.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.knowledge.convertor.ChunkConvertor;
import com.leyue.smartcs.knowledge.dataobject.ChunkDO;
import com.leyue.smartcs.knowledge.mapper.ChunkMapper;

import lombok.RequiredArgsConstructor;

/**
 * 切片Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ChunkGatewayImpl implements ChunkGateway {
    private final ChunkConvertor chunkConvertor;
    private final ChunkMapper chunkMapper;

    
    @Override
    public void save(Chunk chunk) {
        chunkMapper.insert(chunkConvertor.toDO(chunk));
    }
    
    @Override
    public void update(Chunk chunk) {
        chunkMapper.updateById(chunkConvertor.toDO(chunk));
    }
    
    @Override
    public Chunk findById(Long id) {
        return chunkConvertor.toDomain(chunkMapper.selectById(id));
    }
    
    @Override
    public void deleteById(Long id) {
        chunkMapper.deleteById(id);
    }

    @Override
    public void deleteByContentId(Long contentId, StrategyNameEnum strategyName) {
        chunkMapper.delete(new LambdaQueryWrapper<ChunkDO>().eq(ChunkDO::getContentId, contentId).eq(ChunkDO::getStrategyName, strategyName));
    }

    @Override
    public void saveBatch(Long contentId, List<Chunk> chunks, StrategyNameEnum strategyName) {
        chunkMapper.insertBatch(chunks.stream().map(chunkConvertor::toDO).collect(Collectors.toList()));
    }
} 