package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.dataobject.ChunkDO;
import org.mapstruct.Mapper;

/**
 * 切片数据对象转换器
 */
@Mapper(componentModel = "spring")
public interface ChunkConvertor {
    /**
     * DO转Domain
     * @param chunkDO 数据对象
     * @return 领域对象
     */
    Chunk toDomain(ChunkDO chunkDO);
    
    /**
     * Domain转DO
     * @param chunk 领域对象
     * @return 数据对象
     */
    ChunkDO toDO(Chunk chunk);

    /**
     * Domain转DTO
     * @param chunk 领域对象
     * @return 数据对象
     */
    ChunkDTO toDTO(Chunk chunk);
} 