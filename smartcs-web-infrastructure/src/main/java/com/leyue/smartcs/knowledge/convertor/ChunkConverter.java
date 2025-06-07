package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.dataobject.ChunkDO;
import org.mapstruct.Mapper;

/**
 * 切片转换器
 */
@Mapper(componentModel = "spring")
public interface ChunkConverter {
    
    /**
     * 领域对象转DO
     * @param chunk 领域对象
     * @return DO对象
     */
    ChunkDO toDO(Chunk chunk);
    
    /**
     * DO转领域对象
     * @param chunkDO DO对象
     * @return 领域对象
     */
    Chunk toDomain(ChunkDO chunkDO);
    
    /**
     * 领域对象转DTO
     * @param chunk 领域对象
     * @return DTO对象
     */
    ChunkDTO toDTO(Chunk chunk);
    
    /**
     * DTO转领域对象
     * @param chunkDTO DTO对象
     * @return 领域对象
     */
    Chunk toDomain(ChunkDTO chunkDTO);
} 