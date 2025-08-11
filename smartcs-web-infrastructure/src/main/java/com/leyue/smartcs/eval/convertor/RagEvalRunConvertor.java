package com.leyue.smartcs.eval.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.eval.RagEvalRun;
import com.leyue.smartcs.domain.eval.enums.EvaluationRunStatus;
import com.leyue.smartcs.domain.eval.enums.RunType;
import com.leyue.smartcs.eval.dataobject.RagEvalRunDO;
import com.leyue.smartcs.common.context.SpringContextHolder;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * RAG评估运行记录转换器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface RagEvalRunConvertor {

    Logger LOG = LoggerFactory.getLogger(RagEvalRunConvertor.class);
    
    /**
     * DO转领域对象
     * 
     * @param runDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "runType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ragConfigSnapshot", ignore = true)
    @Mapping(target = "modelConfigSnapshot", ignore = true)
    @Mapping(target = "selectedMetrics", ignore = true)
    @Mapping(target = "progressInfo", ignore = true)
    RagEvalRun toDomain(RagEvalRunDO runDO);
    
    /**
     * 领域对象转DO
     * 
     * @param evalRun 领域对象
     * @return 数据对象
     */
    @Mapping(target = "runType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ragConfigSnapshot", ignore = true)
    @Mapping(target = "modelConfigSnapshot", ignore = true)
    @Mapping(target = "selectedMetrics", ignore = true)
    @Mapping(target = "progressInfo", ignore = true)
    RagEvalRunDO toDO(RagEvalRun evalRun);

    /**
     * 兼容命名：领域对象转数据对象
     */
    default RagEvalRunDO toDataObject(RagEvalRun evalRun) {
        return toDO(evalRun);
    }

    /**
     * 兼容命名：数据对象转领域对象
     */
    default RagEvalRun toDomainObject(RagEvalRunDO runDO) {
        return toDomain(runDO);
    }
    
    /**
     * DO转领域对象后处理
     */
    @AfterMapping
    default void afterMappingToDomain(RagEvalRunDO runDO, @MappingTarget RagEvalRun evalRun) {
        ObjectMapper objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
        // 转换运行类型枚举
        if (runDO.getRunType() != null) {
            try {
                evalRun.setRunType(RunType.fromCode(runDO.getRunType()));
            } catch (Exception e) {
                LOG.warn("无效的运行类型: {}, 运行ID: {}", runDO.getRunType(), runDO.getRunId());
            }
        }
        
        // 转换状态枚举
        if (runDO.getStatus() != null) {
            try {
                evalRun.setStatus(EvaluationRunStatus.fromCode(runDO.getStatus()));
            } catch (Exception e) {
                LOG.warn("无效的运行状态: {}, 运行ID: {}", runDO.getStatus(), runDO.getRunId());
            }
        }
        
        // 转换RAG配置快照
        if (runDO.getRagConfigSnapshot() != null) {
            try {
                RagEvalRun.RagConfigSnapshot ragConfig = objectMapper.readValue(
                    runDO.getRagConfigSnapshot(), 
                    RagEvalRun.RagConfigSnapshot.class
                );
                evalRun.setRagConfigSnapshot(ragConfig);
            } catch (Exception e) {
                LOG.error("解析RAG配置快照失败, 运行ID: {}", runDO.getRunId(), e);
            }
        }
        
        // 转换模型配置快照
        if (runDO.getModelConfigSnapshot() != null) {
            try {
                RagEvalRun.ModelConfigSnapshot modelConfig = objectMapper.readValue(
                    runDO.getModelConfigSnapshot(), 
                    RagEvalRun.ModelConfigSnapshot.class
                );
                evalRun.setModelConfigSnapshot(modelConfig);
            } catch (Exception e) {
                LOG.error("解析模型配置快照失败, 运行ID: {}", runDO.getRunId(), e);
            }
        }
        
        // 转换选择的评估指标
        if (runDO.getSelectedMetrics() != null) {
            try {
                List<String> metrics = objectMapper.readValue(
                    runDO.getSelectedMetrics(), 
                    new TypeReference<List<String>>() {}
                );
                evalRun.setSelectedMetrics(metrics);
            } catch (Exception e) {
                LOG.error("解析评估指标列表失败, 运行ID: {}", runDO.getRunId(), e);
            }
        }
        
        // 转换进度信息
        if (runDO.getProgressInfo() != null) {
            try {
                RagEvalRun.ProgressInfo progressInfo = objectMapper.readValue(
                    runDO.getProgressInfo(), 
                    RagEvalRun.ProgressInfo.class
                );
                evalRun.setProgressInfo(progressInfo);
            } catch (Exception e) {
                LOG.error("解析进度信息失败, 运行ID: {}", runDO.getRunId(), e);
            }
        }
    }
    
    /**
     * 领域对象转DO后处理
     */
    @AfterMapping
    default void afterMappingToDO(RagEvalRun evalRun, @MappingTarget RagEvalRunDO runDO) {
        ObjectMapper objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
        // 转换运行类型枚举为字符串
        if (evalRun.getRunType() != null) {
            runDO.setRunType(evalRun.getRunType().getCode());
        }
        
        // 转换状态枚举为字符串
        if (evalRun.getStatus() != null) {
            runDO.setStatus(evalRun.getStatus().getCode());
        }
        
        // 转换RAG配置快照为JSON字符串
        if (evalRun.getRagConfigSnapshot() != null) {
            try {
                String jsonString = objectMapper.writeValueAsString(evalRun.getRagConfigSnapshot());
                runDO.setRagConfigSnapshot(jsonString);
            } catch (Exception e) {
                LOG.error("序列化RAG配置快照失败, 运行ID: {}", evalRun.getRunId(), e);
            }
        }
        
        // 转换模型配置快照为JSON字符串
        if (evalRun.getModelConfigSnapshot() != null) {
            try {
                String jsonString = objectMapper.writeValueAsString(evalRun.getModelConfigSnapshot());
                runDO.setModelConfigSnapshot(jsonString);
            } catch (Exception e) {
                LOG.error("序列化模型配置快照失败, 运行ID: {}", evalRun.getRunId(), e);
            }
        }
        
        // 转换选择的评估指标为JSON字符串
        if (evalRun.getSelectedMetrics() != null) {
            try {
                String jsonString = objectMapper.writeValueAsString(evalRun.getSelectedMetrics());
                runDO.setSelectedMetrics(jsonString);
            } catch (Exception e) {
                LOG.error("序列化评估指标列表失败, 运行ID: {}", evalRun.getRunId(), e);
            }
        }
        
        // 转换进度信息为JSON字符串
        if (evalRun.getProgressInfo() != null) {
            try {
                String jsonString = objectMapper.writeValueAsString(evalRun.getProgressInfo());
                runDO.setProgressInfo(jsonString);
            } catch (Exception e) {
                LOG.error("序列化进度信息失败, 运行ID: {}", evalRun.getRunId(), e);
            }
        }
    }
}