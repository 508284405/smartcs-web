package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.UserKnowledgeBaseRel;
import com.leyue.smartcs.knowledge.dataobject.UserKnowledgeBaseRelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户知识库权限关系数据对象转换器
 */
@Mapper
public interface UserKnowledgeBaseRelConvertor {
    
    UserKnowledgeBaseRelConvertor INSTANCE = Mappers.getMapper(UserKnowledgeBaseRelConvertor.class);
    
    /**
     * DO转Domain
     * @param userKnowledgeBaseRelDO 数据对象
     * @return 领域对象
     */
    UserKnowledgeBaseRel toDomain(UserKnowledgeBaseRelDO userKnowledgeBaseRelDO);
    
    /**
     * Domain转DO
     * @param userKnowledgeBaseRel 领域对象
     * @return 数据对象
     */
    UserKnowledgeBaseRelDO toDO(UserKnowledgeBaseRel userKnowledgeBaseRel);
} 