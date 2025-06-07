package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.knowledge.convertor.ChunkConverter;
import com.leyue.smartcs.knowledge.dataobject.ChunkDO;
import com.leyue.smartcs.knowledge.mapper.ChunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 切片网关实现
 */
@Component
@RequiredArgsConstructor
public class ChunkGatewayImpl implements ChunkGateway {
    
    private final ChunkMapper chunkMapper;
    private final ChunkConverter chunkConverter;
    
    @Override
    public Long save(Chunk chunk) {
        ChunkDO chunkDO = chunkConverter.toDO(chunk);
        chunkMapper.insert(chunkDO);
        return chunkDO.getId();
    }
    
    @Override
    public boolean update(Chunk chunk) {
        ChunkDO chunkDO = chunkConverter.toDO(chunk);
        return chunkMapper.updateById(chunkDO) > 0;
    }
    
    @Override
    public boolean deleteById(Long id) {
        return chunkMapper.deleteById(id) > 0;
    }
    
    @Override
    public Chunk findById(Long id) {
        ChunkDO chunkDO = chunkMapper.selectById(id);
        return chunkDO != null ? chunkConverter.toDomain(chunkDO) : null;
    }
    
    @Override
    public List<Chunk> findByContentId(Long contentId) {
        List<ChunkDO> chunkDOList = chunkMapper.selectByContentId(contentId);
        return chunkDOList.stream()
                .map(chunkConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse<Chunk> findByPage(Long contentId, String keyword, Integer chunkIndex, Integer pageIndex, Integer pageSize) {
        LambdaQueryWrapper<ChunkDO> wrapper = new LambdaQueryWrapper<>();
        
        if (contentId != null) {
            wrapper.eq(ChunkDO::getContentId, contentId);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ChunkDO::getContent, keyword);
        }
        
        if (chunkIndex != null) {
            wrapper.eq(ChunkDO::getChunkIndex, chunkIndex);
        }
        
        wrapper.orderByAsc(ChunkDO::getChunkIndex);
        
        IPage<ChunkDO> page = new Page<>(pageIndex, pageSize);
        IPage<ChunkDO> result = chunkMapper.selectPage(page, wrapper);
        
        List<Chunk> chunks = result.getRecords().stream()
                .map(chunkConverter::toDomain)
                .collect(Collectors.toList());
        
        return PageResponse.of(chunks, (int) result.getTotal(), pageSize, pageIndex);
    }

    @Override
    public List<Long> deleteByContentId(Long contentId) {
        List<ChunkDO> chunkDOList = chunkMapper.selectList(new LambdaQueryWrapper<ChunkDO>().eq(ChunkDO::getContentId, contentId));
        List<Long> ids = chunkDOList.stream().map(ChunkDO::getId).collect(Collectors.toList());
        chunkMapper.deleteByIds(ids);
        return ids;
    }

    @Override
    public List<Long> saveBatch(List<Chunk> chunks) {
        List<ChunkDO> chunkDOList = chunks.stream().map(chunkConverter::toDO).collect(Collectors.toList());
        chunkMapper.insertBatch(chunkDOList);
        return chunkDOList.stream().map(ChunkDO::getId).collect(Collectors.toList());
    }
} 