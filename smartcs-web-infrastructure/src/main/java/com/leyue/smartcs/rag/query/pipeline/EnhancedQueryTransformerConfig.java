package com.leyue.smartcs.rag.query.pipeline;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.rag.query.pipeline.stages.ExpandingStage;
import com.leyue.smartcs.rag.query.pipeline.stages.IntentExtractionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.NormalizationStage;
import com.leyue.smartcs.rag.query.pipeline.stages.RewriteStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SemanticAlignmentStage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 增强查询转换器配置
 * 整合新增的查询转换阶段到现有架构中
 * 
 * @author Claude
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EnhancedQueryTransformerConfig {
    
    private final DynamicModelManager dynamicModelManager;
    private final ObjectMapper objectMapper;
    
    /**
     * 默认指标收集器
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.rag.query.metrics.enabled", havingValue = "true", matchIfMissing = true)
    public DefaultMetricsCollector defaultMetricsCollector() {
        log.info("初始化默认查询转换指标收集器");
        return new DefaultMetricsCollector();
    }
    
    /**
     * 语义对齐阶段
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.rag.query.semantic-alignment.enabled", havingValue = "true", matchIfMissing = true)
    public SemanticAlignmentStage semanticAlignmentStage() {
        log.info("初始化语义对齐阶段");
        return new SemanticAlignmentStage(null); // 使用null作为DictionaryService，实际运行时会自动注入
    }
    
    /**
     * 意图识别阶段
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.rag.query.intent-extraction.enabled", havingValue = "true", matchIfMissing = false)
    public IntentExtractionStage intentExtractionStage() {
        log.info("初始化意图识别与结构化抽取阶段");
        return new IntentExtractionStage(dynamicModelManager, objectMapper, null); // 使用null作为DictionaryService
    }
    
    /**
     * 可检索化改写阶段
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.rag.query.rewrite.enabled", havingValue = "true", matchIfMissing = true)
    public RewriteStage rewriteStage() {
        log.info("初始化可检索化改写阶段");
        return new RewriteStage(null); // 使用null作为DictionaryService
    }
    
    /**
     * 增强查询转换管线
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "smartcs.rag.query.enhanced-pipeline.enabled", havingValue = "true", matchIfMissing = true)
    public QueryTransformerPipeline enhancedQueryTransformerPipeline(
            List<QueryTransformerStage> availableStages,
            DefaultMetricsCollector metricsCollector) {
        
        log.info("初始化增强查询转换管线，可用阶段数量: {}", availableStages.size());
        
        // 构建管线配置
        QueryContext.PipelineConfig pipelineConfig = QueryContext.PipelineConfig.builder()
                .enableNormalization(true)
                .enableIntentRecognition(true)
                .enableExpanding(true)
                .maxQueries(10)
                .keepOriginal(true)
                .dedupThreshold(0.85)
                .fallbackPolicy(QueryContext.FallbackPolicy.SKIP_STAGE)
                .normalizationConfig(QueryContext.NormalizationConfig.builder()
                        .removeStopwords(false)
                        .maxQueryLength(512)
                        .normalizeCase(true)
                        .cleanWhitespace(true)
                        .build())
                .expandingConfig(QueryContext.ExpandingConfig.builder()
                        .n(3)
                        .temperature(0.7)
                        .build())
                .build();
        
        return QueryTransformerPipeline.builder()
                .stages(availableStages)
                .pipelineConfig(pipelineConfig)
                .metricsCollector(metricsCollector)
                .build();
    }
    
    /**
     * 查询转换管线工厂
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.rag.query.pipeline-factory.enabled", havingValue = "true", matchIfMissing = true)
    public QueryTransformerPipelineFactory pipelineFactory(
            List<QueryTransformerStage> availableStages,
            DefaultMetricsCollector metricsCollector) {
        
        log.info("初始化查询转换管线工厂");
        return new QueryTransformerPipelineFactory(availableStages, metricsCollector);
    }
    
    /**
     * 查询转换管线工厂类
     */
    public static class QueryTransformerPipelineFactory {
        
        private final List<QueryTransformerStage> availableStages;
        private final DefaultMetricsCollector metricsCollector;
        
        public QueryTransformerPipelineFactory(List<QueryTransformerStage> availableStages, 
                                             DefaultMetricsCollector metricsCollector) {
            this.availableStages = availableStages;
            this.metricsCollector = metricsCollector;
        }
        
        /**
         * 创建基础管线（只包含标准化）
         */
        public QueryTransformerPipeline createBasicPipeline() {
            List<QueryTransformerStage> basicStages = filterStagesByType(
                Arrays.asList(NormalizationStage.class)
            );
            
            return QueryTransformerPipeline.builder()
                    .stages(basicStages)
                    .pipelineConfig(createBasicConfig())
                    .metricsCollector(metricsCollector)
                    .build();
        }
        
        /**
         * 创建标准管线（标准化 + 语义对齐 + 改写）
         */
        public QueryTransformerPipeline createStandardPipeline() {
            List<QueryTransformerStage> standardStages = filterStagesByType(
                Arrays.asList(
                    NormalizationStage.class,
                    SemanticAlignmentStage.class,
                    RewriteStage.class
                )
            );
            
            return QueryTransformerPipeline.builder()
                    .stages(standardStages)
                    .pipelineConfig(createStandardConfig())
                    .metricsCollector(metricsCollector)
                    .build();
        }
        
        /**
         * 创建完整管线（包含所有阶段）
         */
        public QueryTransformerPipeline createFullPipeline() {
            List<QueryTransformerStage> fullStages = filterStagesByType(
                Arrays.asList(
                    NormalizationStage.class,
                    SemanticAlignmentStage.class,
                    IntentExtractionStage.class,
                    RewriteStage.class,
                    ExpandingStage.class
                )
            );
            
            return QueryTransformerPipeline.builder()
                    .stages(fullStages)
                    .pipelineConfig(createFullConfig())
                    .metricsCollector(metricsCollector)
                    .build();
        }
        
        /**
         * 创建自定义管线
         */
        public QueryTransformerPipeline createCustomPipeline(
                List<Class<? extends QueryTransformerStage>> stageTypes,
                QueryContext.PipelineConfig config) {
            
            List<QueryTransformerStage> customStages = filterStagesByType(stageTypes);
            
            return QueryTransformerPipeline.builder()
                    .stages(customStages)
                    .pipelineConfig(config != null ? config : createStandardConfig())
                    .metricsCollector(metricsCollector)
                    .build();
        }
        
        /**
         * 按类型筛选阶段
         */
        private List<QueryTransformerStage> filterStagesByType(
                List<Class<? extends QueryTransformerStage>> requiredTypes) {
            
            return availableStages.stream()
                    .filter(stage -> requiredTypes.contains(stage.getClass()))
                    .sorted((a, b) -> {
                        // 按预定义顺序排序
                        int indexA = getStageOrder(a.getClass());
                        int indexB = getStageOrder(b.getClass());
                        return Integer.compare(indexA, indexB);
                    })
                    .toList();
        }
        
        /**
         * 获取阶段执行顺序
         */
        private int getStageOrder(Class<? extends QueryTransformerStage> stageClass) {
            if (stageClass == NormalizationStage.class) return 1;
            if (stageClass == SemanticAlignmentStage.class) return 2;
            if (stageClass == IntentExtractionStage.class) return 3;
            if (stageClass == RewriteStage.class) return 4;
            if (stageClass == ExpandingStage.class) return 5;
            return 10; // 其他阶段放在后面
        }
        
        /**
         * 创建基础配置
         */
        private QueryContext.PipelineConfig createBasicConfig() {
            return QueryContext.PipelineConfig.builder()
                    .enableNormalization(true)
                    .enableIntentRecognition(false)
                    .enableExpanding(false)
                    .maxQueries(3)
                    .keepOriginal(true)
                    .dedupThreshold(0.9)
                    .fallbackPolicy(QueryContext.FallbackPolicy.ORIGINAL_QUERY_ONLY)
                    .build();
        }
        
        /**
         * 创建标准配置
         */
        private QueryContext.PipelineConfig createStandardConfig() {
            return QueryContext.PipelineConfig.builder()
                    .enableNormalization(true)
                    .enableIntentRecognition(false)
                    .enableExpanding(false)
                    .maxQueries(5)
                    .keepOriginal(true)
                    .dedupThreshold(0.85)
                    .fallbackPolicy(QueryContext.FallbackPolicy.SKIP_STAGE)
                    .build();
        }
        
        /**
         * 创建完整配置
         */
        private QueryContext.PipelineConfig createFullConfig() {
            return QueryContext.PipelineConfig.builder()
                    .enableNormalization(true)
                    .enableIntentRecognition(true)
                    .enableExpanding(true)
                    .maxQueries(10)
                    .keepOriginal(true)
                    .dedupThreshold(0.85)
                    .fallbackPolicy(QueryContext.FallbackPolicy.SKIP_STAGE)
                    .normalizationConfig(QueryContext.NormalizationConfig.builder()
                            .removeStopwords(false)
                            .maxQueryLength(512)
                            .normalizeCase(true)
                            .cleanWhitespace(true)
                            .build())
                    .expandingConfig(QueryContext.ExpandingConfig.builder()
                            .n(3)
                            .temperature(0.7)
                            .build())
                    .build();
        }
        
        /**
         * 获取可用阶段信息
         */
        public List<String> getAvailableStageNames() {
            return availableStages.stream()
                    .map(QueryTransformerStage::getName)
                    .toList();
        }
        
        /**
         * 获取阶段统计信息
         */
        public DefaultMetricsCollector.GlobalMetrics getGlobalMetrics() {
            return metricsCollector.getGlobalMetrics();
        }
        
        /**
         * 重置指标收集器
         */
        public void resetMetrics() {
            metricsCollector.reset();
        }
        
        /**
         * 打印指标报告
         */
        public void printMetricsReport() {
            metricsCollector.printMetricsReport();
        }
    }
}