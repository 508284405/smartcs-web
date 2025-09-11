package com.leyue.smartcs.rag.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.model.service.DefaultModelService;
import com.leyue.smartcs.rag.metrics.SlotFillingMetricsCollector;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerPipeline;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.services.PhoneticCorrectionService;
import com.leyue.smartcs.rag.query.pipeline.services.PrefixCompletionService;
import com.leyue.smartcs.rag.query.pipeline.services.SynonymRecallService;
import com.leyue.smartcs.rag.query.pipeline.stages.ExpandingStage;
import com.leyue.smartcs.rag.query.pipeline.stages.ExpansionStrategyStage;
import com.leyue.smartcs.rag.query.pipeline.stages.IntentExtractionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.NormalizationStage;
import com.leyue.smartcs.rag.query.pipeline.stages.PhoneticCorrectionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.PrefixCompletionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.RewriteStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SemanticAlignmentStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SlotFillingStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SynonymRecallStage;

import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * QueryTransformer配置类
 * 负责根据配置创建和注册QueryTransformer相关的Bean
 * 
 * 设计原则：
 * 1. 基于配置灵活创建QueryTransformer实例
 * 2. 支持管线化和传统模式的切换
 * 3. 提供合适的默认配置
 * 4. 集成现有的意图识别和模型管理服务
 * 
 * @author Claude
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class QueryTransformerConfiguration {
    
    private final DynamicModelManager dynamicModelManager;
    private final ClassificationDomainService classificationDomainService;
    private final SlotFillingMetricsCollector slotFillingMetricsCollector;
    private final DefaultModelService defaultModelService;
    
    @Autowired(required = false)
    private DictionaryService dictionaryService;
    
    /**
     * 创建管线化的QueryTransformer（优先使用）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "smartcs.rag.query-transformer.enable-pipeline", havingValue = "true", matchIfMissing = true)
    public QueryTransformer pipelineQueryTransformer() {
        log.info("创建管线化QueryTransformer");
        
        // 获取默认LLM模型ID
        Long modelId = getDefaultModelId();
        log.info("QueryTransformerPipeline使用的模型ID: {}", modelId);
        
        return QueryTransformerPipeline.builder()
                .stages(createDefaultStages())
                .pipelineConfig(createDefaultPipelineConfig())
                .metricsCollector(createMetricsCollector())
                .modelId(modelId)
                .build();
    }
    
    /**
     * 创建传统的意图感知QueryTransformer（降级方案）
     */
    @Bean
    @ConditionalOnProperty(value = "smartcs.rag.query-transformer.enable-pipeline", havingValue = "false")
    public QueryTransformer intentAwareQueryTransformer() {
        log.info("创建传统意图感知QueryTransformer");
        
        // 获取默认LLM模型ID
        Long modelId = getDefaultModelId();
        log.info("传统QueryTransformer使用的模型ID: {}", modelId);
        
        // 使用管线式QueryTransformer实现
        return QueryTransformerPipeline.builder()
                .stages(createDefaultStages())
                .pipelineConfig(createDefaultPipelineConfig())
                .metricsCollector(createMetricsCollector())
                .modelId(modelId)
                .build();
    }
    
    /**
     * 创建默认的处理阶段（集成字典服务，使用动态LLM配置）
     */
    private List<QueryTransformerStage> createDefaultStages() {
        List<QueryTransformerStage> stages = new ArrayList<>();
        
        // 1. 标准化阶段（集成字典服务）
        if (dictionaryService != null) {
            stages.add(new NormalizationStage(dictionaryService));
            log.debug("标准化阶段已集成字典服务");
        } else {
            stages.add(new NormalizationStage());
            log.debug("标准化阶段使用内置数据（字典服务不可用）");
        }
        
        // 2. 拼音纠错阶段（集成字典服务）
        PhoneticCorrectionService phoneticService = new PhoneticCorrectionService(0.8);
        if (dictionaryService != null) {
            stages.add(new PhoneticCorrectionStage(phoneticService, dictionaryService));
            log.debug("拼音纠错阶段已集成字典服务");
        } else {
            stages.add(new PhoneticCorrectionStage(phoneticService, null));
            log.debug("拼音纠错阶段使用内置数据（字典服务不可用）");
        }
        
        // 3. 前缀补全阶段（新增，集成字典服务）
        PrefixCompletionService prefixService = new PrefixCompletionService(null, dictionaryService);
        stages.add(new PrefixCompletionStage(prefixService,null));
        if (dictionaryService != null) {
            log.debug("前缀补全阶段已集成字典服务");
        } else {
            log.debug("前缀补全阶段使用内置数据（字典服务不可用）");
        }
        
        // 4. 同义词召回阶段（新增，集成字典服务）
        SynonymRecallService synonymService = new SynonymRecallService(dictionaryService);
        stages.add(new SynonymRecallStage(synonymService, dictionaryService));
        if (dictionaryService != null) {
            log.debug("同义词召回阶段已集成字典服务");
        } else {
            log.debug("同义词召回阶段使用内置数据（字典服务不可用）");
        }
        
        // 5. 语义对齐阶段（M2增强，集成字典服务）
        if (dictionaryService != null) {
            stages.add(new SemanticAlignmentStage(dictionaryService));
            log.debug("语义对齐阶段已集成字典服务");
        } else {
            stages.add(new SemanticAlignmentStage(null));
            log.debug("语义对齐阶段使用内置数据（字典服务不可用）");
        }
        
        // 6. 意图抽取阶段（M2增强，集成字典服务和动态模型管理器）
        if (dictionaryService != null) {
            stages.add(new IntentExtractionStage(dynamicModelManager, new ObjectMapper(), dictionaryService));
            log.debug("意图抽取阶段已集成字典服务和动态模型管理器");
        } else {
            stages.add(new IntentExtractionStage(dynamicModelManager, new ObjectMapper(), null));
            log.debug("意图抽取阶段使用内置数据（字典服务不可用）");
        }
        
        // 7. 槽位填充阶段（集成字典服务和指标收集器）
        if (dictionaryService != null) {
            stages.add(new SlotFillingStage(dictionaryService, new ObjectMapper(), slotFillingMetricsCollector));
            log.debug("槽位填充阶段已集成字典服务和指标收集器");
        } else {
            log.debug("槽位填充阶段跳过（字典服务不可用）");
        }
        
        // 8. 可检索化改写阶段（M3实现，集成字典服务）
        if (dictionaryService != null) {
            stages.add(new RewriteStage(dictionaryService));
            log.debug("可检索化改写阶段已集成字典服务");
        } else {
            stages.add(new RewriteStage(null));
            log.debug("可检索化改写阶段使用内置数据（字典服务不可用）");
        }
        
        // 8. 查询扩展阶段（使用模型提供者）
        stages.add(new ExpandingStage(dynamicModelManager));
        
        // 9. 检索增强策略阶段（M3实现，集成字典服务和动态模型管理器）
        if (dictionaryService != null) {
            stages.add(new ExpansionStrategyStage(dynamicModelManager, dictionaryService));
            log.debug("检索增强策略阶段已集成字典服务和动态模型管理器");
        } else {
            stages.add(new ExpansionStrategyStage(dynamicModelManager, null));
            log.debug("检索增强策略阶段使用内置数据（字典服务不可用）");
        }
        
        log.info("创建默认处理阶段完成: stageCount={}, 字典服务状态={}, 动态LLM支持=已启用", 
                stages.size(), dictionaryService != null ? "已集成" : "未集成");
        return stages;
    }
    
    /**
     * 创建默认的管线配置
     */
    private QueryContext.PipelineConfig createDefaultPipelineConfig() {
        return QueryContext.PipelineConfig.builder()
                .enableNormalization(true)
                .enableExpanding(true)
                .enableIntentRecognition(true)
                .enableSlotFilling(true)  // 启用槽位填充
                .maxQueries(10)
                .keepOriginal(true)
                .dedupThreshold(0.85)
                .fallbackPolicy(QueryContext.FallbackPolicy.SKIP_STAGE)
                .expandingConfig(QueryContext.ExpandingConfig.builder()
                        .n(3)
                        .temperature(0.7)
                        .build())
                .normalizationConfig(QueryContext.NormalizationConfig.builder()
                        .removeStopwords(false)
                        .maxQueryLength(512)
                        .normalizeCase(true)
                        .cleanWhitespace(true)
                        .build())
                .slotFillingConfig(QueryContext.SlotFillingConfig.builder()
                        .maxClarificationAttempts(3)
                        .completenessThreshold(0.8)
                        .blockRetrievalOnMissing(true)
                        .enableSmartQuestionGeneration(true)
                        .timeoutMs(5000)
                        .build())
                .build();
    }
    
    /**
     * 获取默认的模型ID
     * 如果无法获取默认LLM模型，则抛出异常
     */
    private Long getDefaultModelId() {
        return defaultModelService.getDefaultLlmModelId();
    }
    
    /**
     * 创建指标收集器
     */
    private QueryContext.MetricsCollector createMetricsCollector() {
        return new QueryContext.MetricsCollector() {
            @Override
            public void recordStageStart(String stageName, int inputQueryCount) {
                log.debug("阶段开始: stage={}, inputCount={}", stageName, inputQueryCount);
            }
            
            @Override
            public void recordStageComplete(String stageName, int outputQueryCount, long elapsedMs) {
                log.debug("阶段完成: stage={}, outputCount={}, elapsedMs={}", 
                         stageName, outputQueryCount, elapsedMs);
            }
            
            @Override
            public void recordStageFailure(String stageName, Throwable error, long elapsedMs) {
                log.warn("阶段失败: stage={}, error={}, elapsedMs={}", 
                        stageName, error.getMessage(), elapsedMs);
            }
            
            @Override
            public void recordStageSkipped(String stageName, String reason) {
                log.debug("阶段跳过: stage={}, reason={}", stageName, reason);
            }
            
            @Override
            public void recordTokensConsumption(String stageName, int inputTokens, int outputTokens) {
                log.debug("Token消耗: stage={}, input={}, output={}", 
                         stageName, inputTokens, outputTokens);
            }
            
            @Override
            public void recordCostConsumption(String stageName, double cost) {
                log.debug("成本消耗: stage={}, cost=${}", stageName, cost);
            }
        };
    }
    
    
    /**
     * QueryTransformer阶段工厂Bean
     */
    @Bean
    public QueryTransformerStageFactory queryTransformerStageFactory() {
        return new QueryTransformerStageFactory(dynamicModelManager, classificationDomainService);
    }
    
    /**
     * QueryTransformer阶段工厂实现
     */
    @RequiredArgsConstructor
    public static class QueryTransformerStageFactory {
        private final DynamicModelManager dynamicModelManager;
        private final ClassificationDomainService classificationDomainService;
        
        /**
         * 根据配置创建处理阶段（使用动态LLM配置）
         */
        public List<QueryTransformerStage> createStages(QueryTransformerConfig config, Long modelId, DictionaryService dictionaryService) {
            List<QueryTransformerStage> stages = new ArrayList<>();
            
            // 按配置添加阶段
            if (config.isEnableNormalization()) {
                if (dictionaryService != null) {
                    stages.add(new NormalizationStage(dictionaryService));
                } else {
                    stages.add(new NormalizationStage());
                }
            }
            
            if (config.isEnableSemanticAlignment()) {
                stages.add(new SemanticAlignmentStage(dictionaryService));
            }
            
            if (config.isEnableIntentExtraction()) {
                // 使用动态模型管理器和字典服务，支持运行时切换LLM和多租户字典
                stages.add(new IntentExtractionStage(dynamicModelManager, new ObjectMapper(), dictionaryService));
            }
            
            if (config.isEnableRetrievability()) {
                // 可检索化改写阶段（M3实现）
                stages.add(new RewriteStage(dictionaryService));
            }
            
            if (config.isEnableExpanding()) {
                // 使用动态模型管理器，支持运行时切换LLM
                stages.add(new ExpandingStage(dynamicModelManager));
            }
            
            if (config.isEnableExpansionStrategy()) {
                // 检索增强策略阶段（M3实现）
                try {
                    stages.add(new ExpansionStrategyStage(dynamicModelManager, dictionaryService));
                } catch (Exception e) {
                    log.warn("创建检索增强策略阶段失败，跳过此阶段", e);
                }
            }
            
            return stages;
        }
    }
    
    /**
     * 配置类（简化版，实际应使用RagComponentConfig.QueryTransformerConfig）
     */
    private static class QueryTransformerConfig {
        public boolean isEnableNormalization() { return true; }
        public boolean isEnableSemanticAlignment() { return true; }
        public boolean isEnableIntentExtraction() { return true; }
        public boolean isEnableRetrievability() { return true; }
        public boolean isEnableExpanding() { return true; }
        public boolean isEnableExpansionStrategy() { return false; }
    }
}
