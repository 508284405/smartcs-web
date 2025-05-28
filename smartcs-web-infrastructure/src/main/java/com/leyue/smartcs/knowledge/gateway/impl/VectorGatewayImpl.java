package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.Vector;
import com.leyue.smartcs.domain.knowledge.gateway.VectorGateway;
import com.leyue.smartcs.knowledge.convertor.VectorConvertor;
import com.leyue.smartcs.knowledge.mapper.VectorMapper;
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
} 