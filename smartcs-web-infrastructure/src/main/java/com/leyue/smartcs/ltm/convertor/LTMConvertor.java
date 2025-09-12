package com.leyue.smartcs.ltm.convertor;

import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;
import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;
import com.leyue.smartcs.ltm.dataobject.EpisodicMemoryDO;
import com.leyue.smartcs.ltm.dataobject.SemanticMemoryDO;
import com.leyue.smartcs.ltm.dataobject.ProceduralMemoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.List;

@Mapper
public interface LTMConvertor {
    LTMConvertor INSTANCE = Mappers.getMapper(LTMConvertor.class);

    // Episodic
    EpisodicMemoryDO toDO(EpisodicMemory entity);
    EpisodicMemory toEntity(EpisodicMemoryDO data);

    // Semantic
    SemanticMemoryDO toDO(SemanticMemory entity);
    SemanticMemory toEntity(SemanticMemoryDO data);

    // Procedural
    ProceduralMemoryDO toDO(ProceduralMemory entity);
    ProceduralMemory toEntity(ProceduralMemoryDO data);
}

