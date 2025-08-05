package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.Vector;
import com.leyue.smartcs.knowledge.dataobject.VectorDO;
import org.mapstruct.Mapper;

/**
 * 向量数据对象转换器
 */
@Mapper(componentModel = "spring")
public interface VectorConvertor {
    
    /**
     * DO转Domain
     * @param vectorDO 数据对象
     * @return 领域对象
     */
    Vector toDomain(VectorDO vectorDO);
    
    /**
     * Domain转DO
     * @param vector 领域对象
     * @return 数据对象
     */
    VectorDO toDO(Vector vector);
} 