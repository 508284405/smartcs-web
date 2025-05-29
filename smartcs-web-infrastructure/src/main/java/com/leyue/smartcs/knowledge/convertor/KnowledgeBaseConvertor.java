package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseCreateCmd;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseDTO;
import com.leyue.smartcs.knowledge.dataobject.KnowledgeBaseDO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 知识库数据对象转换器
 */
@Mapper(componentModel = "spring")
public interface KnowledgeBaseConvertor {
    
    /**
     * DO转Domain
     * @param knowledgeBaseDO 数据对象
     * @return 领域对象
     */
    KnowledgeBase toDomain(KnowledgeBaseDO knowledgeBaseDO);

    /**
     * Cmd转Domain
     * @param cmd 命令对象
     * @return 领域对象
     */
    KnowledgeBase toDomain(KnowledgeBaseCreateCmd cmd);
    
    /**
     * Domain转DO
     * @param knowledgeBase 领域对象
     * @return 数据对象
     */
    KnowledgeBaseDO toDO(KnowledgeBase knowledgeBase);

    /**
     * 将领域模型转换为DTO
     */
    KnowledgeBaseDTO toDTO(KnowledgeBase knowledgeBase);

    /**
     * DO转DTO
     * @param knowledgeBaseDO 数据对象
     * @return DTO对象
     */
    KnowledgeBaseDTO toDTO(KnowledgeBaseDO knowledgeBaseDO);

    /**
     * 批量将数据对象转换为DTO
     */
    List<KnowledgeBaseDTO> toDTO(List<KnowledgeBaseDO> knowledgeBaseDOs);

    /**
     * 批量将数据对象转换为领域模型
     */
    List<KnowledgeBase> toDomainList(List<KnowledgeBaseDO> knowledgeBaseDOs);

    /**
     * 批量将领域模型转换为DTO
     */
    List<KnowledgeBaseDTO> toDTOList(List<KnowledgeBase> knowledgeBases);
} 