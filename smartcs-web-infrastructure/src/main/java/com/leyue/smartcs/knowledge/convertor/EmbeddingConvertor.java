package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.knowledge.dataobject.EmbeddingDO;
import com.leyue.smartcs.knowledge.dto.EmbeddingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 向量对象转换接口
 */
@Mapper(componentModel = "spring")
public interface EmbeddingConvertor {

    EmbeddingConvertor INSTANCE = Mappers.getMapper(EmbeddingConvertor.class);
    
    /**
 * 将领域模型转换为数据对象
 */
@Mapping(target = "vector", expression = "java(objectToBytes(embedding.getVector()))")
EmbeddingDO toDataObject(Embedding embedding);

/**
 * 将Object类型的向量转换为byte[]
 */
default byte[] objectToBytes(Object vector) {
    if (vector == null) {
        return null;
    }
    if (vector instanceof byte[]) {
        return (byte[]) vector;
    }
    if (vector instanceof String) {
        return java.util.Base64.getDecoder().decode((String) vector);
    }
    throw new IllegalArgumentException("Unsupported vector type: " + vector.getClass().getName());
}
    
    /**
     * 将数据对象转换为领域模型
     */
    Embedding toDomain(EmbeddingDO embeddingDO);
    
    /**
     * 将领域模型转换为DTO
     */
    @Mapping(target = "vector", expression = "java(java.util.Base64.getEncoder().encodeToString((byte[])embedding.getVector()))")
    EmbeddingDTO toDTO(Embedding embedding);
    
    /**
     * 批量将数据对象转换为领域模型
     */
    List<Embedding> toDomainList(List<EmbeddingDO> embeddingDOs);
    
    /**
     * 批量将领域模型转换为DTO
     */
    List<EmbeddingDTO> toDTOList(List<Embedding> embeddings);
} 