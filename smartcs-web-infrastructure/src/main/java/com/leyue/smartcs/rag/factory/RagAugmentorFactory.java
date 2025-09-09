package com.leyue.smartcs.rag.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.leyue.smartcs.model.gateway.ModelProvider;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import com.leyue.smartcs.rag.config.WebSearchProperties;
import com.leyue.smartcs.rag.content.retriever.SqlQueryContentRetriever;
import com.leyue.smartcs.rag.database.service.NlpToSqlService;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerPipeline;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.stages.ExpandingStage;
import com.leyue.smartcs.rag.query.pipeline.stages.IntentExtractionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.NormalizationStage;
import com.leyue.smartcs.rag.query.pipeline.stages.PhoneticCorrectionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.PrefixCompletionStage;
import com.leyue.smartcs.rag.query.pipeline.stages.RewriteStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SemanticAlignmentStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SlotFillingStage;
import com.leyue.smartcs.rag.query.pipeline.stages.SynonymRecallStage;
import com.leyue.smartcs.rag.query.pipeline.services.PhoneticCorrectionService;
import com.leyue.smartcs.rag.query.pipeline.services.PrefixCompletionService;
import com.leyue.smartcs.rag.query.pipeline.services.SynonymRecallService;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.rag.metrics.SlotFillingMetricsCollector;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.community.web.search.searxng.SearXNGWebSearchEngine;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RAG组件装配工厂
 * <p>
 * 负责根据配置动态创建和装配各种RAG组件，包括查询转换器、内容检索器、
 * 内容聚合器等。通过依赖注入的ModelProvider获取所需的模型实例，
 * 避免直接依赖具体的模型管理器实现。
 * </p>
 * 
 * <h3>职责范围:</h3>
 * <ul>
 *   <li>RAG组件的创建和配置</li>
 *   <li>查询处理管线的动态装配</li>
 *   <li>多种内容检索器的组合</li>
 *   <li>内容聚合和排序策略的配置</li>
 * </ul>
 * 
 * <h3>设计特点:</h3>
 * <ul>
 *   <li>通过ModelProvider接口获取模型，避免循环依赖</li>
 *   <li>支持自定义配置和默认配置</li>
 *   <li>组件缓存机制提升性能</li>
 *   <li>模块化设计便于扩展</li>
 * </ul>
 * 
 * @author Claude
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RagAugmentorFactory {
    
    private final ModelProvider modelProvider;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final SearXNGWebSearchEngine searxngWebSearchEngine;
    private final WebSearchProperties webSearchProperties;
    private final JdbcTemplate jdbcTemplate;
    private final NlpToSqlService nlpToSqlService;
    private final DictionaryService dictionaryService;
    private final DynamicModelManager dynamicModelManager;
    private final ObjectMapper objectMapper;
    private final SlotFillingMetricsCollector slotFillingMetricsCollector;
    
    // 缓存RAG组件实例，避免重复创建
    private final Map<Long, RetrievalAugmentor> retrievalAugmentorCache = new ConcurrentHashMap<>();
    private final Map<Long, ContentInjector> contentInjectorCache = new ConcurrentHashMap<>();
    private final Map<Long, QueryTransformer> queryTransformerCache = new ConcurrentHashMap<>();
    private final Map<Long, QueryRouter> queryRouterCache = new ConcurrentHashMap<>();
    private final Map<Long, ReRankingContentAggregator> contentAggregatorCache = new ConcurrentHashMap<>();
    
    /**
     * 根据模型ID创建RetrievalAugmentor（使用默认配置）
     * 
     * @param modelId 模型ID
     * @return RetrievalAugmentor实例
     */
    public RetrievalAugmentor createRetrievalAugmentor(Long modelId) {
        return createRetrievalAugmentor(modelId, null);
    }
    
    /**
     * 根据模型ID和配置创建RetrievalAugmentor
     * 
     * @param modelId 模型ID
     * @param ragConfig RAG配置，如果为null则使用默认配置
     * @return RetrievalAugmentor实例
     */
    public RetrievalAugmentor createRetrievalAugmentor(Long modelId, RagComponentConfig ragConfig) {
        // 为了支持配置变化，我们不缓存带有自定义配置的实例
        if (ragConfig != null) {
            log.debug("创建自定义配置的RetrievalAugmentor实例: modelId={}, ragConfig={}", modelId, ragConfig);
            
            return DefaultRetrievalAugmentor.builder()
                    .queryRouter(createQueryRouter(modelId, ragConfig.getQueryRouterOrDefault(), 
                            ragConfig.getKnowledgeSearchOrDefault(), ragConfig.getWebSearchOrDefault(), 
                            ragConfig.getSqlQueryOrDefault()))
                    .queryTransformer(createQueryTransformer(modelId, ragConfig.getQueryTransformerOrDefault()))
                    .contentAggregator(createContentAggregator(modelId, ragConfig.getContentAggregatorOrDefault()))
                    .contentInjector(createContentInjector(modelId, ragConfig.getContentInjectorOrDefault()))
                    .build();
        }
        
        return retrievalAugmentorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的RetrievalAugmentor实例: modelId={}", id);
            
            return DefaultRetrievalAugmentor.builder()
                    .queryRouter(createQueryRouter(id))
                    .queryTransformer(createQueryTransformer(id))
                    .contentAggregator(createContentAggregator(id))
                    .contentInjector(createContentInjector(id))
                    .build();
        });
    }
    
    /**
     * 根据模型ID创建ContentInjector（使用默认配置）
     * 
     * @param modelId 模型ID
     * @return ContentInjector实例
     */
    public ContentInjector createContentInjector(Long modelId) {
        return createContentInjector(modelId, null);
    }
    
    /**
     * 根据模型ID和配置创建ContentInjector
     * 
     * @param modelId 模型ID
     * @param config 内容注入器配置，如果为null则使用默认配置
     * @return ContentInjector实例
     */
    public ContentInjector createContentInjector(Long modelId, RagComponentConfig.ContentInjectorConfig config) {
        if (config != null) {
            log.debug("创建自定义配置的ContentInjector实例: modelId={}, config={}", modelId, config);
            
            PromptTemplate promptTemplate = null;
            if (config.getPromptTemplate() != null && !config.getPromptTemplate().trim().isEmpty()) {
                promptTemplate = PromptTemplate.from(config.getPromptTemplate());
            }
            
            return new DefaultContentInjector(promptTemplate, config.getMetadataKeysToInclude());
        }
        
        return contentInjectorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的ContentInjector实例: modelId={}", id);
            return new DefaultContentInjector();
        });
    }
    
    /**
     * 根据模型ID创建QueryTransformer（使用默认配置）
     * 
     * @param modelId 模型ID
     * @return QueryTransformer实例
     */
    public QueryTransformer createQueryTransformer(Long modelId) {
        return createQueryTransformer(modelId, null);
    }
    
    /**
     * 根据模型ID和配置创建QueryTransformer
     * 
     * @param modelId 模型ID
     * @param config 查询转换器配置，如果为null则使用默认配置
     * @return QueryTransformer实例
     */
    public QueryTransformer createQueryTransformer(Long modelId, RagComponentConfig.QueryTransformerConfig config) {
        if (config != null) {
            log.debug("创建自定义配置的QueryTransformer实例: modelId={}, config={}", modelId, config);
            return createSimpleQueryTransformer(modelId, config);
        }
        
        return queryTransformerCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的QueryTransformer实例（启用意图识别）: modelId={}", id);
            
            // 创建默认配置
            RagComponentConfig.QueryTransformerConfig defaultConfig = RagComponentConfig.QueryTransformerConfig.builder()
                    .n(5)
                    // 避免在未配置 querySelector 的情况下产生多条查询导致重排序歧义
                    .maxQueries(1)
                    .intentRecognitionEnabled(true)
                    .defaultChannel("web")
                    .defaultTenant("default")
                    .modelId(id)
                    .build();
            
            return createSimpleQueryTransformer(id, defaultConfig);
        });
    }
    
    /**
     * 创建简化的QueryTransformer实现
     * 
     * @param modelId 模型ID
     * @param config 配置
     * @return QueryTransformer实例
     */
    private QueryTransformer createSimpleQueryTransformer(Long modelId, RagComponentConfig.QueryTransformerConfig config) {
        // 创建管线实现
        ChatModel chatModel = modelProvider.getChatModel(modelId);
        QueryTransformerPipeline pipeline = createQueryTransformerPipeline(chatModel, config);
        return pipeline;
    }
    
    /**
     * 创建查询转换器管线
     * 
     * @param chatModel 聊天模型
     * @param config 配置
     * @return QueryTransformerPipeline实例
     */
    private QueryTransformerPipeline createQueryTransformerPipeline(ChatModel chatModel, 
                                                                   RagComponentConfig.QueryTransformerConfig config) {
        log.debug("构建查询转换器管线: config={}", config);
        
        // 创建管线配置
        QueryContext.PipelineConfig.PipelineConfigBuilder pipelineConfigBuilder = QueryContext.PipelineConfig.builder()
                .enableNormalization(config.isEnableNormalization())
                .enableExpanding(config.isEnableExpanding())
                .enableIntentRecognition(config.isIntentRecognitionEnabled())
                .enableSlotFilling(true)
                .enablePhoneticCorrection(config.isEnablePhoneticCorrection())
                .enablePrefixCompletion(config.isEnablePrefixCompletion())
                .enableSynonymRecall(config.isEnableSynonymRecall())
                .maxQueries(config.getMaxQueries())
                .keepOriginal(config.isKeepOriginal())
                .dedupThreshold(config.getDedupThreshold());
                
        // 设置降级策略
        try {
            if (config.getFallbackPolicy() != null) {
                QueryContext.FallbackPolicy fallbackPolicy = 
                    QueryContext.FallbackPolicy.valueOf(config.getFallbackPolicy());
                pipelineConfigBuilder.fallbackPolicy(fallbackPolicy);
            }
        } catch (IllegalArgumentException e) {
            log.warn("无效的降级策略，使用默认策略: {}", config.getFallbackPolicy());
            pipelineConfigBuilder.fallbackPolicy(QueryContext.FallbackPolicy.SKIP_STAGE);
        }
        
        // 设置各种配置
        if (config.getNormalizationConfig() != null) {
            QueryContext.NormalizationConfig normalizationConfig = createNormalizationConfig(
                    config.getNormalizationConfigOrDefault());
            pipelineConfigBuilder.normalizationConfig(normalizationConfig);
        }
        
        if (config.getExpandingConfig() != null) {
            QueryContext.ExpandingConfig expandingConfig = createExpandingConfig(
                    config.getExpandingConfigOrDefault(), config);
            pipelineConfigBuilder.expandingConfig(expandingConfig);
        }

        // 设置拼音改写配置
        if (config.getPhoneticConfigOrDefault() != null) {
            QueryContext.PhoneticConfig phoneticConfig = QueryContext.PhoneticConfig.builder()
                    .minConfidence(config.getPhoneticConfigOrDefault().getMinConfidence())
                    .maxCandidates(config.getPhoneticConfigOrDefault().getMaxCandidates())
                    .build();
            pipelineConfigBuilder.phoneticConfig(phoneticConfig);
        }

        // 设置前缀补全配置
        if (config.getPrefixConfigOrDefault() != null) {
            QueryContext.PrefixConfig prefixConfig = QueryContext.PrefixConfig.builder()
                    .minPrefixLength(config.getPrefixConfigOrDefault().getMinPrefixLength())
                    .maxCandidates(config.getPrefixConfigOrDefault().getMaxCandidates())
                    .onlyShortQuery(config.getPrefixConfigOrDefault().getOnlyShortQuery() != null && config.getPrefixConfigOrDefault().getOnlyShortQuery())
                    .shortQueryMaxLen(config.getPrefixConfigOrDefault().getShortQueryMaxLen())
                    .build();
            pipelineConfigBuilder.prefixConfig(prefixConfig);
        }

        // 设置近义词召回配置
        if (config.getSynonymRecallConfigOrDefault() != null) {
            QueryContext.SynonymConfig synonymConfig = QueryContext.SynonymConfig.builder()
                    .embeddingModelId(config.getSynonymRecallConfigOrDefault().getEmbeddingModelId())
                    .topK(config.getSynonymRecallConfigOrDefault().getTopK())
                    .simThreshold(config.getSynonymRecallConfigOrDefault().getSimThreshold())
                    .build();
            pipelineConfigBuilder.synonymConfig(synonymConfig);
        }
        
        QueryContext.PipelineConfig pipelineConfig = pipelineConfigBuilder.build();
        
        // 创建处理阶段
        List<QueryTransformerStage> stages = createPipelineStages(chatModel, pipelineConfig);
        
        // 创建指标收集器
        QueryContext.MetricsCollector metricsCollector = createMetricsCollector();
        
        // 构建管线
        return QueryTransformerPipeline.builder()
                .stages(stages)
                .pipelineConfig(pipelineConfig)
                .metricsCollector(metricsCollector)
                .defaultTenant(config.getDefaultTenant())
                .defaultChannel(config.getDefaultChannel())
                .build();
    }
    
    /**
     * 创建管线处理阶段
     * 
     * @param chatModel 聊天模型
     * @param config 管线配置
     * @return 处理阶段列表
     */
    private List<QueryTransformerStage> createPipelineStages(ChatModel chatModel, 
                                                           QueryContext.PipelineConfig config) {
        List<QueryTransformerStage> stages = new ArrayList<>();

        // 添加标准化阶段
        if (config.isEnableNormalization()) {
            stages.add(new NormalizationStage());
        }

        // 语义对齐（接入字典服务）
        stages.add(new SemanticAlignmentStage(dictionaryService));

        // 意图抽取阶段（启用并注入动态模型与字典服务）
        if (config.isEnableIntentRecognition()) {
            stages.add(new IntentExtractionStage(dynamicModelManager, objectMapper, dictionaryService));
        }

        // 拼音改写阶段
        if (config.isEnablePhoneticCorrection()) {
            double minConf = config.getPhoneticConfig() != null ? config.getPhoneticConfig().getMinConfidence() : 0.6;
            PhoneticCorrectionService phoneticService = new PhoneticCorrectionService(minConf);
            stages.add(new PhoneticCorrectionStage(phoneticService, dictionaryService));
        }

        // 可检索化改写阶段（接入字典服务）
        stages.add(new RewriteStage(dictionaryService));

        // 前缀补全阶段
        if (config.isEnablePrefixCompletion()) {
            PrefixCompletionService prefixService = new PrefixCompletionService(Collections.emptyList(), dictionaryService);
            stages.add(new PrefixCompletionStage(prefixService, dictionaryService));
        }

        // 近义词召回阶段
        if (config.isEnableSynonymRecall()) {
            SynonymRecallService synService = new SynonymRecallService(dictionaryService);
            stages.add(new SynonymRecallStage(synService, dictionaryService));
        }

        // 添加扩展阶段
        if (config.isEnableExpanding()) {
            stages.add(new ExpandingStage(modelProvider));
        }

        // 槽位填充阶段（基于意图模板生成澄清问题）
        if (config.isEnableSlotFilling()) {
            stages.add(new SlotFillingStage(dictionaryService, objectMapper, slotFillingMetricsCollector));
        }

        log.debug("创建管线处理阶段完成: stageCount={}, 动态LLM支持=已启用", stages.size());
        
        return stages;
    }
    
    /**
     * 创建标准化配置
     * 
     * @param config 标准化配置
     * @return QueryContext.NormalizationConfig实例
     */
    private QueryContext.NormalizationConfig createNormalizationConfig(
            RagComponentConfig.QueryTransformerConfig.NormalizationConfig config) {
        return QueryContext.NormalizationConfig.builder()
                .removeStopwords(config.isRemoveStopwords())
                .maxQueryLength(config.getMaxQueryLength())
                .normalizeCase(config.isNormalizeCase())
                .cleanWhitespace(config.isCleanWhitespace())
                .build();
    }
    
    /**
     * 创建扩展配置
     * 
     * @param expandingConfig 扩展配置
     * @param parentConfig 父配置
     * @return QueryContext.ExpandingConfig实例
     */
    private QueryContext.ExpandingConfig createExpandingConfig(
            RagComponentConfig.QueryTransformerConfig.ExpandingConfig expandingConfig,
            RagComponentConfig.QueryTransformerConfig parentConfig) {
        return QueryContext.ExpandingConfig.builder()
                .n(expandingConfig.getActualN(parentConfig.getN()))
                .promptTemplate(expandingConfig.getActualPromptTemplate(parentConfig.getPromptTemplate()))
                .temperature(expandingConfig.getTemperature())
                .build();
    }
    
    /**
     * 创建指标收集器
     * 
     * @return QueryContext.MetricsCollector实例
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
                log.debug("Token消耗: stage={}, inputTokens={}, outputTokens={}", 
                        stageName, inputTokens, outputTokens);
            }
            
            @Override
            public void recordCostConsumption(String stageName, double cost) {
                log.debug("成本消耗: stage={}, cost=${:.4f}", stageName, cost);
            }
        };
    }
    
    /**
     * 根据模型ID创建QueryRouter（使用默认配置）
     * 
     * @param modelId 模型ID
     * @return QueryRouter实例
     */
    public QueryRouter createQueryRouter(Long modelId) {
        return createQueryRouter(modelId, null, null, null, null);
    }
    
    /**
     * 根据模型ID和配置创建QueryRouter
     * 
     * @param modelId 模型ID
     * @param config 查询路由器配置
     * @param knowledgeSearchConfig 知识库搜索配置
     * @param webSearchConfig Web搜索配置
     * @param sqlQueryConfig SQL查询配置
     * @return QueryRouter实例
     */
    public QueryRouter createQueryRouter(Long modelId, RagComponentConfig.QueryRouterConfig config, 
            RagComponentConfig.KnowledgeSearchConfig knowledgeSearchConfig, 
            RagComponentConfig.WebSearchConfig webSearchConfig, 
            RagComponentConfig.SqlQueryConfig sqlQueryConfig) {
        
        if (config != null) {
            log.debug("创建自定义配置的QueryRouter实例: modelId={}, config={}", modelId, config);
            // 使用组件级模型ID，未指定时回退到会话级 modelId
            Long actualModelId = config.getModelId() != null ? config.getModelId() : modelId;
            ChatModel chatModel = modelProvider.getChatModel(actualModelId);
            
            // 根据配置创建内容检索器
            Map<ContentRetriever, String> retrievers = new HashMap<>();
            
            if (config.getEnableKnowledgeRetrieval()) {
                // 知识库检索
                // 使用知识库检索配置的模型ID，未指定时回退到会话级 modelId
                Long knowledgeModelId = knowledgeSearchConfig.getModelId() != null ? 
                    knowledgeSearchConfig.getModelId() : modelId;
                ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(embeddingStore)
                        .displayName("知识库检索")
                        .dynamicFilter(null)
                        .dynamicMaxResults(null)
                        .dynamicMinScore(null)
                        .embeddingModel(modelProvider.getEmbeddingModel(knowledgeModelId))
                        .filter(null)
                        .maxResults(knowledgeSearchConfig.getTopK()) 
                        .minScore(knowledgeSearchConfig.getScoreThreshold()) // 0.0-1.0
                        .build();
                retrievers.put(contentRetriever, "知识库检索");
            }
            
            if (config.getEnableWebSearch()) {
                // 检查全局Web搜索开关
                if (webSearchProperties.isEnabled()) {
                    ContentRetriever webContentRetriever = createWebContentRetriever(webSearchConfig);
                    if (webContentRetriever != null) {
                        retrievers.put(webContentRetriever, "Web搜索");
                    }
                } else {
                    log.info("配置启用Web搜索但全局开关已禁用，跳过Web搜索检索器注册 - modelId: {}", modelId);
                }
            }
            
            if (config.getEnableSqlQuery()) {
                // 数据库检索
                ContentRetriever sqlQueryContentRetriever = createSqlQueryContentRetriever(modelId, modelId, sqlQueryConfig);
                retrievers.put(sqlQueryContentRetriever, "数据库查询");
            }
            
            return LanguageModelQueryRouter.builder()
                    .chatModel(chatModel)
                    .promptTemplate(config.getPromptTemplate() != null ? PromptTemplate.from(config.getPromptTemplate()) : null)
                    .retrieverToDescription(retrievers)
                    .build();
        }
        
        return queryRouterCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的QueryRouter实例: modelId={}", id);
            ChatModel chatModel = modelProvider.getChatModel(id);
            
            // 创建内容检索器
            Map<ContentRetriever, String> retrievers = new HashMap<>();
            
            // 知识库检索始终启用
            ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.from(embeddingStore);
            retrievers.put(contentRetriever, "知识库检索");
            
            // 根据全局开关决定是否启用Web搜索
            if (webSearchProperties.isEnabled()) {
                ContentRetriever webContentRetriever = createWebContentRetriever();
                if (webContentRetriever != null) {
                    retrievers.put(webContentRetriever, "Web搜索");
                }
            } else {
                log.info("Web搜索已被全局开关禁用，跳过Web搜索检索器注册");
            }
            
            // SQL查询检索器始终启用
            ContentRetriever sqlQueryContentRetriever = createSqlQueryContentRetriever(id, id, null);
            retrievers.put(sqlQueryContentRetriever, "数据库查询");
            
            return LanguageModelQueryRouter.builder()
                    .chatModel(chatModel)
                    .promptTemplate(null)
                    .retrieverToDescription(retrievers)
                    .build();
        });
    }
    
    /**
     * 根据模型ID创建ReRankingContentAggregator（使用默认配置）
     * 
     * @param modelId 模型ID
     * @return ReRankingContentAggregator实例
     */
    public ReRankingContentAggregator createContentAggregator(Long modelId) {
        return createContentAggregator(modelId, null);
    }
    
    /**
     * 根据模型ID和配置创建ReRankingContentAggregator
     * 
     * @param modelId 模型ID
     * @param config 内容聚合器配置
     * @return ReRankingContentAggregator实例
     */
    public ReRankingContentAggregator createContentAggregator(Long modelId, RagComponentConfig.ContentAggregatorConfig config) {
        if (config != null) {
            log.debug("创建自定义配置的ReRankingContentAggregator实例: modelId={}, config={}", modelId, config);
            
            // 尝试设置评分模型，如果获取失败则使用无评分模型的配置
            try {
                ScoringModel scoringModel = null;
                if (config.getScoringModelId() != null) {
                    log.debug("尝试获取ScoringModel: scoringModelId={}", config.getScoringModelId());
                    scoringModel = modelProvider.getScoringModel(config.getScoringModelId());
                } else if (modelId != null) {
                    // 如果未指定评分模型ID，尝试使用当前模型ID
                    log.debug("未指定评分模型ID，尝试使用当前模型ID: modelId={}", modelId);
                    scoringModel = modelProvider.getScoringModel(modelId);
                }
                
                if (scoringModel != null) {
                    log.debug("成功获取ScoringModel，启用重排序功能");
                    return ReRankingContentAggregator.builder()
                            .maxResults(config.getMaxResults())
                            .minScore(config.getMinScore())
                            // 选择原始查询用于重排序（管线会将原始查询置于首位）
                            .scoringModel(scoringModel)
                            .build();
                } else {
                    log.warn("ScoringModel不可用，将使用基础的ContentAggregator（无重排序）");
                    return ReRankingContentAggregator.builder()
                            .maxResults(config.getMaxResults())
                            .minScore(config.getMinScore())
                            .build();
                }
            } catch (Exception e) {
                log.warn("获取ScoringModel失败，将使用基础的ContentAggregator（无重排序）: {}", e.getMessage());
                return ReRankingContentAggregator.builder()
                        .maxResults(config.getMaxResults())
                        .minScore(config.getMinScore())
                        .build();
            }
        }
        
        return contentAggregatorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的ReRankingContentAggregator实例: modelId={}", id);
            
            try {
                // 尝试使用当前模型ID创建ScoringModel以启用重排序
                ScoringModel scoringModel = modelProvider.getScoringModel(id);
                if (scoringModel != null) {
                    log.debug("默认配置成功获取ScoringModel，启用重排序功能");
                    return ReRankingContentAggregator.builder()
                            .maxResults(5)
                            .minScore(0.5)
                            .scoringModel(scoringModel)
                            .build();
                }
            } catch (Exception e) {
                log.debug("默认配置获取ScoringModel失败，将使用基础聚合器: {}", e.getMessage());
            }
            
            // 降级方案：不使用重排序
            log.debug("默认配置使用基础ContentAggregator（无重排序）");
            return ReRankingContentAggregator.builder()
                    .maxResults(5)
                    .minScore(0.5)
                    .build();
        });
    }
    
    /**
     * 创建Web内容检索器（使用默认配置）
     * 
     * @return ContentRetriever实例
     */
    private ContentRetriever createWebContentRetriever() {
        return createWebContentRetriever(null);
    }
    
    /**
     * 创建Web内容检索器
     * 
     * @param config Web搜索配置
     * @return ContentRetriever实例
     */
    private ContentRetriever createWebContentRetriever(RagComponentConfig.WebSearchConfig config) {
        // 防御性检查：全局开关禁用时直接返回null
        if (!webSearchProperties.isEnabled()) {
            log.debug("Web搜索全局开关已禁用，返回null ContentRetriever");
            return null;
        }
        
        try {
            if (config != null) {
                return WebSearchContentRetriever.builder()
                        .webSearchEngine(searxngWebSearchEngine)
                        .maxResults(config.getMaxResults())
                        .build();
            }
            
            return WebSearchContentRetriever.builder()
                    .webSearchEngine(searxngWebSearchEngine)
                    .maxResults(10)
                    .build();
        } catch (Exception e) {
            log.warn("创建Web内容检索器失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建SQL查询内容检索器
     * 
     * @param chatModelId 聊天模型ID
     * @param embeddingModelId 嵌入模型ID
     * @param config SQL查询配置
     * @return ContentRetriever实例
     */
    private ContentRetriever createSqlQueryContentRetriever(Long chatModelId, Long embeddingModelId, 
            RagComponentConfig.SqlQueryConfig config) {
        return new SqlQueryContentRetriever(jdbcTemplate, nlpToSqlService, chatModelId, embeddingModelId);
    }
    
    /**
     * 清除指定模型的RAG组件缓存
     * 
     * @param modelId 模型ID
     */
    public void clearRagComponentCache(Long modelId) {
        log.info("清除RAG组件缓存: modelId={}", modelId);
        retrievalAugmentorCache.remove(modelId);
        contentInjectorCache.remove(modelId);
        queryTransformerCache.remove(modelId);
        queryRouterCache.remove(modelId);
        contentAggregatorCache.remove(modelId);
    }
    
    /**
     * 清除所有RAG组件缓存
     */
    public void clearAllRagComponentCache() {
        log.info("清除所有RAG组件缓存");
        retrievalAugmentorCache.clear();
        contentInjectorCache.clear();
        queryTransformerCache.clear();
        queryRouterCache.clear();
        contentAggregatorCache.clear();
    }
    
    /**
     * 获取RAG组件缓存统计信息
     * 
     * @return 缓存大小映射
     */
    public Map<String, Integer> getRagComponentCacheStats() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("retrievalAugmentorCache", retrievalAugmentorCache.size());
        stats.put("contentInjectorCache", contentInjectorCache.size());
        stats.put("queryTransformerCache", queryTransformerCache.size());
        stats.put("queryRouterCache", queryRouterCache.size());
        stats.put("contentAggregatorCache", contentAggregatorCache.size());
        return stats;
    }
}
