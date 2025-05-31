package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.Vector;
import com.leyue.smartcs.domain.knowledge.gateway.VectorGateway;
import com.leyue.smartcs.knowledge.convertor.VectorConvertor;
import com.leyue.smartcs.knowledge.dataobject.VectorDO;
import com.leyue.smartcs.knowledge.mapper.VectorMapper;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 向量Gateway实现
 */
@Component
public class VectorGatewayImpl implements VectorGateway {
    
    @Autowired
    private VectorMapper vectorMapper;
    
    @Override
    public void save(Vector vector) {
        vectorMapper.insert(VectorConvertor.INSTANCE.toDO(vector));
    }
    
    @Override
    public void update(Vector vector) {
        vectorMapper.updateById(VectorConvertor.INSTANCE.toDO(vector));
    }
    
    @Override
    public Vector findById(Long id) {
        return VectorConvertor.INSTANCE.toDomain(vectorMapper.selectById(id));
    }
    
    @Override
    public void deleteById(Long id) {
        vectorMapper.deleteById(id);
    }

    @Override
    public List<Vector> saveBatch(List<Vector> vectors) {
        List<VectorDO> dos = vectors.stream().map(VectorConvertor.INSTANCE::toDO).collect(Collectors.toList());
        vectorMapper.insertBatch(dos);
        return dos.stream().map(VectorConvertor.INSTANCE::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByChunkIds(List<Long> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return;
        }
        vectorMapper.delete(new LambdaQueryWrapper<VectorDO>().in(VectorDO::getChunkId, chunkIds));
    }
} 