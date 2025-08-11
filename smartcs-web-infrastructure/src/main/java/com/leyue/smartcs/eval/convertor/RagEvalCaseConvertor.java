package com.leyue.smartcs.eval.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.eval.RagEvalCase;
import com.leyue.smartcs.domain.eval.enums.DifficultyTag;
import com.leyue.smartcs.eval.dataobject.RagEvalCaseDO;
import com.leyue.smartcs.common.context.SpringContextHolder;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * RAG评估测试用例转换器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface RagEvalCaseConvertor {

    Logger LOG = LoggerFactory.getLogger(RagEvalCaseConvertor.class);
    
    /**
     * DO转领域对象
     * 
     * @param caseDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "difficultyTag", ignore = true)
    @Mapping(target = "goldEvidenceRefs", ignore = true)
    @Mapping(target = "groundTruthContexts", ignore = true)
    RagEvalCase toDomain(RagEvalCaseDO caseDO);
    
    /**
     * 领域对象转DO
     * 
     * @param evalCase 领域对象
     * @return 数据对象
     */
    @Mapping(target = "difficultyTag", ignore = true)
    @Mapping(target = "goldEvidenceRefs", ignore = true)
    @Mapping(target = "groundTruthContexts", ignore = true)
    RagEvalCaseDO toDO(RagEvalCase evalCase);

    /**
     * 兼容命名：领域对象转数据对象
     */
    default RagEvalCaseDO toDataObject(RagEvalCase evalCase) {
        return toDO(evalCase);
    }

    /**
     * 兼容命名：数据对象转领域对象
     */
    default RagEvalCase toDomainObject(RagEvalCaseDO caseDO) {
        return toDomain(caseDO);
    }
    
    /**
     * DO转领域对象后处理
     */
    @AfterMapping
    default void afterMappingToDomain(RagEvalCaseDO caseDO, @MappingTarget RagEvalCase evalCase) {
        ObjectMapper objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
        // 转换难度标签枚举
        if (caseDO.getDifficultyTag() != null) {
            try {
                evalCase.setDifficultyTag(DifficultyTag.fromCode(caseDO.getDifficultyTag()));
            } catch (Exception e) {
                LOG.warn("无效的难度标签: {}, 测试用例ID: {}", caseDO.getDifficultyTag(), caseDO.getCaseId());
            }
        }
        
        // 转换标准证据引用
        if (caseDO.getGoldEvidenceRefs() != null) {
            try {
                List<RagEvalCase.EvidenceReference> evidenceRefs = objectMapper.readValue(
                    caseDO.getGoldEvidenceRefs(), 
                    new TypeReference<List<RagEvalCase.EvidenceReference>>() {}
                );
                evalCase.setGoldEvidenceRefs(evidenceRefs);
            } catch (Exception e) {
                LOG.error("解析标准证据引用失败, 测试用例ID: {}", caseDO.getCaseId(), e);
            }
        }
        
        // 转换标准上下文
        if (caseDO.getGroundTruthContexts() != null) {
            try {
                List<RagEvalCase.GroundTruthContext> groundTruthContexts = objectMapper.readValue(
                    caseDO.getGroundTruthContexts(), 
                    new TypeReference<List<RagEvalCase.GroundTruthContext>>() {}
                );
                evalCase.setGroundTruthContexts(groundTruthContexts);
            } catch (Exception e) {
                LOG.error("解析标准上下文失败, 测试用例ID: {}", caseDO.getCaseId(), e);
            }
        }
    }
    
    /**
     * 领域对象转DO后处理
     */
    @AfterMapping
    default void afterMappingToDO(RagEvalCase evalCase, @MappingTarget RagEvalCaseDO caseDO) {
        ObjectMapper objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
        // 转换难度标签枚举为字符串
        if (evalCase.getDifficultyTag() != null) {
            caseDO.setDifficultyTag(evalCase.getDifficultyTag().getCode());
        }
        
        // 转换标准证据引用为JSON字符串
        if (evalCase.getGoldEvidenceRefs() != null) {
            try {
                String jsonString = objectMapper.writeValueAsString(evalCase.getGoldEvidenceRefs());
                caseDO.setGoldEvidenceRefs(jsonString);
            } catch (Exception e) {
                LOG.error("序列化标准证据引用失败, 测试用例ID: {}", evalCase.getCaseId(), e);
            }
        }
        
        // 转换标准上下文为JSON字符串
        if (evalCase.getGroundTruthContexts() != null) {
            try {
                String jsonString = objectMapper.writeValueAsString(evalCase.getGroundTruthContexts());
                caseDO.setGroundTruthContexts(jsonString);
            } catch (Exception e) {
                LOG.error("序列化标准上下文失败, 测试用例ID: {}", evalCase.getCaseId(), e);
            }
        }
    }
}