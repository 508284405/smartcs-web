package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.Vector;

/**
 * 向量Gateway接口
 */
public interface VectorGateway {
    
    /**
     * 保存向量
     * @param vector 向量对象
     */
    void save(Vector vector);
    
    /**
     * 更新向量
     * @param vector 向量对象
     */
    void update(Vector vector);
    
    /**
     * 根据ID查找向量
     * @param id 向量ID
     * @return 向量对象
     */
    Vector findById(Long id);
    
    /**
     * 根据ID删除向量
     * @param id 向量ID
     */
    void deleteById(Long id);
} 