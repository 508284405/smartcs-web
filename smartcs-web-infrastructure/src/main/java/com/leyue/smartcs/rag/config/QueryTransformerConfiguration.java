package com.leyue.smartcs.rag.config;

import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import com.leyue.smartcs.intent.ai.IntentClassificationAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.rag.query.IntentAwareQueryTransformer;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerPipeline;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.stages.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * 创建管线化的QueryTransformer（优先使用）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "smartcs.rag.query-transformer.enable-pipeline", havingValue = "true", matchIfMissing = true)
    public QueryTransformer pipelineQueryTransformer() {
        log.info("创建管线化QueryTransformer");
        
        return QueryTransformerPipeline.builder()
                .stages(createDefaultStages())
                .pipelineConfig(createDefaultPipelineConfig())
                .metricsCollector(createMetricsCollector())
                .build();
    }
    
    /**
     * 创建传统的意图感知QueryTransformer（降级方案）
     */
    @Bean
    @ConditionalOnProperty(value = "smartcs.rag.query-transformer.enable-pipeline", havingValue = "false")
    public QueryTransformer intentAwareQueryTransformer() {
        log.info("创建传统意图感知QueryTransformer");
        
        // 从配置中获取参数，这里使用默认值
        Long defaultModelId = 1L; // 应从配置或DynamicModelManager获取
        int queryExpansionCount = 5;
        boolean intentRecognitionEnabled = true;
        String defaultChannel = "web";
        String defaultTenant = "default";
        
        return new IntentAwareQueryTransformer(
                classificationDomainService,
                dynamicModelManager,
                defaultModelId,
                queryExpansionCount,
                intentRecognitionEnabled,
                defaultChannel,
                defaultTenant
        );
    }
    
    /**
     * 创建默认的处理阶段
     */
    private List<QueryTransformerStage> createDefaultStages() {
        List<QueryTransformerStage> stages = new ArrayList<>();
        
        // 获取默认的ChatModel
        ChatModel chatModel = getDefaultChatModel();
        
        // 1. 标准化阶段（现有实现）
        stages.add(new NormalizationStage());
        
        // 2. 语义对齐阶段（新增）
        stages.add(new SemanticAlignmentStage());
        
        // 3. 意图抽取阶段（新增）
        IntentClassificationAiService aiService = AiServices.builder(IntentClassificationAiService.class)
                .chatModel(chatModel)
                .build();
        stages.add(new IntentExtractionStage(aiService, new ObjectMapper()));
        
        // 4. 可检索化改写阶段（预留，暂不启用）
        // stages.add(new RetrievabilityStage(chatModel));
        
        // 5. 查询扩展阶段（现有实现）
        stages.add(new ExpandingStage(chatModel));
        
        // 6. 检索增强策略阶段（新增，默认禁用）
        // stages.add(new ExpansionStrategyStage(chatModel));
        
        log.debug("创建默认处理阶段: stageCount={}", stages.size());
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
                .build();
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
     * 获取默认的ChatModel
     */
    private ChatModel getDefaultChatModel() {
        try {
            // 从DynamicModelManager获取默认模型
            Long defaultModelId = 1L; // 应从配置获取
            return dynamicModelManager.getChatModel(defaultModelId);
        } catch (Exception e) {
            log.warn("获取默认ChatModel失败，使用降级方案", e);
            // 这里应该有一个降级的ChatModel实现
            throw new IllegalStateException("无法获取默认ChatModel", e);
        }
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
         * 根据配置创建处理阶段
         */
        public List<QueryTransformerStage> createStages(QueryTransformerConfig config, Long modelId) {
            List<QueryTransformerStage> stages = new ArrayList<>();
            
            ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
            
            // 按配置添加阶段
            if (config.isEnableNormalization()) {
                stages.add(new NormalizationStage());
            }
            
            if (config.isEnableSemanticAlignment()) {
                stages.add(new SemanticAlignmentStage());
            }
            
            if (config.isEnableIntentExtraction()) {
                IntentClassificationAiService aiService = AiServices.builder(IntentClassificationAiService.class)
                        .chatModel(chatModel)
                        .build();
                stages.add(new IntentExtractionStage(aiService, new ObjectMapper()));
            }
            
            // 预留：可检索化改写阶段暂不启用（类未实现）
            
            if (config.isEnableExpanding()) {
                stages.add(new ExpandingStage(chatModel));
            }
            
            if (config.isEnableExpansionStrategy()) {
                stages.add(new ExpansionStrategyStage(chatModel));
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
