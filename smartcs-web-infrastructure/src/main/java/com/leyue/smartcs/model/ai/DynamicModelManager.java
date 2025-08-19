package com.leyue.smartcs.model.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import com.leyue.smartcs.model.convertor.ProviderConvertor;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import com.leyue.smartcs.model.mapper.ProviderMapper;
import com.leyue.smartcs.rag.SmartChatService;
import com.leyue.smartcs.rag.StructuredChatServiceAi;
import com.leyue.smartcs.rag.config.WebSearchProperties;
import com.leyue.smartcs.rag.content.retriever.SqlQueryContentRetriever;
import com.leyue.smartcs.rag.database.service.NlpToSqlService;
import com.leyue.smartcs.rag.query.IntentAwareQueryTransformer;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerPipeline;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.stages.ExpandingStage;
import com.leyue.smartcs.rag.query.pipeline.stages.NormalizationStage;

import dev.langchain4j.community.web.search.searxng.SearXNGWebSearchEngine;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
// import dev.langchain4j.model.openai.OpenAiScoringModel; // 1.1.0版本暂未提供
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
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态模型管理器
 * 负责根据modelId动态获取和缓存ChatModel和StreamingChatModel实例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicModelManager {

    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;
    private final ProviderMapper providerMapper;
    private final ProviderConvertor providerConvertor;
    private final ChatMemoryStore chatMemoryStore;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final SearXNGWebSearchEngine searxngWebSearchEngine;
    private final WebSearchProperties webSearchProperties;
    private final ClassificationDomainService classificationDomainService;
    private final JdbcTemplate jdbcTemplate;
    private final NlpToSqlService nlpToSqlService;
    
    // 缓存模型实例，避免重复创建
    private final Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
    private final Map<Long, ScoringModel> scoringModelCache = new ConcurrentHashMap<>();
    
    // 缓存RAG组件实例，避免重复创建
    private final Map<Long, RetrievalAugmentor> retrievalAugmentorCache = new ConcurrentHashMap<>();
    private final Map<Long, ContentInjector> contentInjectorCache = new ConcurrentHashMap<>();
    private final Map<Long, QueryTransformer> queryTransformerCache = new ConcurrentHashMap<>();
    private final Map<Long, QueryRouter> queryRouterCache = new ConcurrentHashMap<>();
    private final Map<Long, ReRankingContentAggregator> contentAggregatorCache = new ConcurrentHashMap<>();

    /**
     * 根据模型ID获取ChatModel
     * 
     * @param modelId 模型ID
     * @return ChatModel实例
     */
    public ChatModel getChatModel(Long modelId) {
        return chatModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建ChatModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildChatModel(provider, model);
        });
    }

    /**
     * 根据模型ID获取StreamingChatModel
     * 
     * @param modelId 模型ID
     * @return StreamingChatModel实例
     */
    public StreamingChatModel getStreamingChatModel(Long modelId) {
        return streamingChatModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建StreamingChatModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildStreamingChatModel(provider, model);
        });
    }

    /**
     * 根据模型ID获取EmbeddingModel
     * 
     * @param modelId 模型ID
     * @return EmbeddingModel实例
     */
    public EmbeddingModel getEmbeddingModel(Long modelId) {
        return embeddingModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建EmbeddingModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildEmbeddingModel(provider, model);
        });
    }

    /**
     * 根据模型ID获取ScoringModel
     * 
     * @param modelId 模型ID
     * @return ScoringModel实例
     */
    public ScoringModel getScoringModel(Long modelId) {
        return scoringModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建ScoringModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildScoringModel(provider, model);
        });
    }

    /**
     * 检查模型是否支持推理
     * 
     * @param modelId 模型ID
     * @return 是否支持推理
     */
    public boolean supportsInference(Long modelId) {
        try {
            Model model = getModel(modelId);
            return model != null && model.getStatus() != null 
                   && model.getStatus().getCode().equals("active");
        } catch (Exception e) {
            log.warn("检查模型推理支持失败: modelId={}", modelId, e);
            return false;
        }
    }

    /**
     * 检查模型是否支持流式推理
     * 
     * @param modelId 模型ID
     * @return 是否支持流式推理
     */
    public boolean supportsStreaming(Long modelId) {
        try {
            StreamingChatModel streamingChatModel = getStreamingChatModel(modelId);
            return streamingChatModel != null;
        } catch (Exception e) {
            log.warn("检查模型流式推理支持失败: modelId={}", modelId, e);
            return false;
        }
    }

    /**
     * 清除指定模型的缓存
     * 
     * @param modelId 模型ID
     */
    public void clearModelCache(Long modelId) {
        log.info("清除模型缓存: modelId={}", modelId);
        chatModelCache.remove(modelId);
        streamingChatModelCache.remove(modelId);
        embeddingModelCache.remove(modelId);
        scoringModelCache.remove(modelId);
    }

    /**
     * 清除所有模型缓存
     */
    public void clearAllCache() {
        log.info("清除所有模型缓存");
        chatModelCache.clear();
        streamingChatModelCache.clear();
        embeddingModelCache.clear();
        scoringModelCache.clear();
        
        // 同时清除RAG组件缓存
        clearAllRagComponentCache();
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存大小映射
     */
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("chatModelCache", chatModelCache.size());
        stats.put("streamingChatModelCache", streamingChatModelCache.size());
        stats.put("embeddingModelCache", embeddingModelCache.size());
        stats.put("scoringModelCache", scoringModelCache.size());
        return stats;
    }

    /**
     * 获取模型信息
     */
    private Model getModel(Long modelId) {
        return modelGateway.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在: modelId=" + modelId));
    }

    /**
     * 获取提供商信息
     */
    private Provider getProvider(Long providerId) {
        return providerGateway.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("提供商不存在: providerId=" + providerId));
    }

    /**
     * 构建ChatModel实例
     * 使用即时解密获取API Key，使用后立即释放引用
     */
    private ChatModel buildChatModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            // 获取Provider DO用于解密
            ProviderDO providerDO = providerMapper.selectById(provider.getId());
            if (providerDO == null) {
                throw new IllegalStateException("提供商数据不存在: " + provider.getId());
            }
            
            // 即时解密API Key
            String apiKey = providerConvertor.decryptApiKey(providerDO);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API Key未设置或解密失败: " + provider.getId());
            }
            
            try {
                // 构建模型实例
                ChatModel chatModel = OpenAiChatModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(apiKey)
                        .modelName(model.getLabel())
                        .build();
                
                log.debug("ChatModel构建完成，Provider ID: {}, Model: {}", provider.getId(), model.getLabel());
                return chatModel;
                
            } finally {
                // 立即清空明文API Key引用
                apiKey = null;
            }
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建StreamingChatModel实例
     * 使用即时解密获取API Key，使用后立即释放引用
     */
    private StreamingChatModel buildStreamingChatModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            // 获取Provider DO用于解密
            ProviderDO providerDO = providerMapper.selectById(provider.getId());
            if (providerDO == null) {
                throw new IllegalStateException("提供商数据不存在: " + provider.getId());
            }
            
            // 即时解密API Key
            String apiKey = providerConvertor.decryptApiKey(providerDO);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API Key未设置或解密失败: " + provider.getId());
            }
            
            try {
                // 构建模型实例
                StreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(apiKey)
                        .modelName(model.getLabel())
                        .build();
                
                log.debug("StreamingChatModel构建完成，Provider ID: {}, Model: {}", provider.getId(), model.getLabel());
                return streamingChatModel;
                
            } finally {
                // 立即清空明文API Key引用
                apiKey = null;
            }
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建EmbeddingModel实例
     * 使用即时解密获取API Key，使用后立即释放引用
     */
    private EmbeddingModel buildEmbeddingModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            // 获取Provider DO用于解密
            ProviderDO providerDO = providerMapper.selectById(provider.getId());
            if (providerDO == null) {
                throw new IllegalStateException("提供商数据不存在: " + provider.getId());
            }
            
            // 即时解密API Key
            String apiKey = providerConvertor.decryptApiKey(providerDO);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API Key未设置或解密失败: " + provider.getId());
            }
            
            try {
                // 构建模型实例
                EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(apiKey)
                        .modelName(model.getLabel())
                        .build();
                
                log.debug("EmbeddingModel构建完成，Provider ID: {}, Model: {}", provider.getId(), model.getLabel());
                return embeddingModel;
                
            } finally {
                // 立即清空明文API Key引用
                apiKey = null;
            }
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建ScoringModel实例
     * 使用基于LLM的自定义ScoringModel实现，通过ChatModel进行相关性打分
     */
    private ScoringModel buildScoringModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            try {
                ChatModel chatModel = buildChatModel(provider, model);
                return new LlmBasedScoringModel(chatModel);
            } catch (Exception e) {
                log.warn("创建LlmBasedScoringModel失败，返回null: {}", e.getMessage());
                return null;
            }
        }
        log.warn("不支持的提供商类型用于ScoringModel: {}", provider.getProviderType().getKey());
        return null;
    }

    /**
     * 创建模型推理服务 - 集成完整RAG能力
     * 基于LangChain4j框架的声明式AI服务创建
     * 
     * @param modelId 模型ID
     * @param knowledgeIds 知识库ID列表 (当前版本暂未使用，为未来扩展预留)
     * @return ModelInferenceService实例
     */
    public ModelInferenceService createModelInferenceService(Long modelId, java.util.List<Long> knowledgeIds) {
        log.info("创建ModelInferenceService: modelId={}, knowledgeIds={}", modelId, knowledgeIds);
        
        try {
            // 获取模型对应的ChatModel和StreamingChatModel
            ChatModel chatModel = getChatModel(modelId);
            StreamingChatModel streamingChatModel = getStreamingChatModel(modelId);
            
            // 创建RAG增强器
            RetrievalAugmentor retrievalAugmentor = createRetrievalAugmentor(modelId);
            
            // 使用LangChain4j AiServices框架创建推理服务
            return AiServices.builder(ModelInferenceService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .retrievalAugmentor(retrievalAugmentor)
                    .build();
            
        } catch (Exception e) {
            log.error("创建ModelInferenceService失败: modelId={}, knowledgeIds={}", modelId, knowledgeIds, e);
            throw new RuntimeException("无法创建ModelInferenceService: " + e.getMessage(), e);
        }
    }

    /**
     * 根据模型ID创建RetrievalAugmentor（使用默认配置）
     */
    public RetrievalAugmentor createRetrievalAugmentor(Long modelId) {
        return createRetrievalAugmentor(modelId, null);
    }

    /**
     * 根据模型ID和配置创建RetrievalAugmentor
     * 
     * @param modelId 模型ID
     * @param ragConfig RAG配置，如果为null则使用默认配置
     */
    public RetrievalAugmentor createRetrievalAugmentor(Long modelId, RagComponentConfig ragConfig) {
        // 为了支持配置变化，我们不缓存带有自定义配置的实例
        if (ragConfig != null) {
            log.debug("创建自定义配置的RetrievalAugmentor实例: modelId={}, ragConfig={}", modelId, ragConfig);
            
            return DefaultRetrievalAugmentor.builder()
                    .queryRouter(createQueryRouter(modelId, ragConfig.getQueryRouterOrDefault(),ragConfig.getKnowledgeSearchOrDefault(),ragConfig.getWebSearchOrDefault(),ragConfig.getSqlQueryOrDefault()))
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
     */
    public ContentInjector createContentInjector(Long modelId) {
        return createContentInjector(modelId, null);
    }

    /**
     * 根据模型ID和配置创建ContentInjector
     * 
     * @param modelId 模型ID
     * @param config 内容注入器配置，如果为null则使用默认配置
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
     */
    public QueryTransformer createQueryTransformer(Long modelId) {
        return createQueryTransformer(modelId, null);
    }

    /**
     * 根据模型ID和配置创建QueryTransformer
     * 
     * @param modelId 模型ID
     * @param config 查询转换器配置，如果为null则使用默认配置
     */
    public QueryTransformer createQueryTransformer(Long modelId, RagComponentConfig.QueryTransformerConfig config) {
        if (config != null) {
            log.debug("创建自定义配置的QueryTransformer实例: modelId={}, config={}", modelId, config);
            // 使用组件级模型ID，未指定时回退到会话级 modelId
            Long actualModelId = config.getModelId() != null ? config.getModelId() : modelId;
            ChatModel chatModel = getChatModel(actualModelId);
            
            // 检查是否启用管线化处理
            if (config.isEnablePipeline()) {
                log.debug("创建QueryTransformerPipeline: modelId={}, pipelineEnabled=true", actualModelId);
                return createQueryTransformerPipeline(chatModel, config);
            }
            
            // 传统模式：检查是否启用意图识别
            if (config.isIntentRecognitionEnabled()) {
                log.debug("创建IntentAwareQueryTransformer: modelId={}, intentEnabled=true", actualModelId);
                return new IntentAwareQueryTransformer(
                    classificationDomainService,
                    this,
                    actualModelId,
                    config.getN(),
                    true,
                    config.getDefaultChannel(),
                    config.getDefaultTenant()
                );
            } else {
                // 使用标准的查询扩展器
                return ExpandingQueryTransformer.builder()
                        .chatModel(chatModel)
                        .n(config.getN())
                        .promptTemplate(config.getPromptTemplate() != null ? 
                                PromptTemplate.from(config.getPromptTemplate()) : null)
                        .build();
            }
        }
        
        return queryTransformerCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的QueryTransformer实例（启用意图识别）: modelId={}", id);
            // 默认启用意图识别
            return new IntentAwareQueryTransformer(
                classificationDomainService,
                this,
                id,
                5, // 默认扩展数量
                true, // 默认启用意图识别
                "web", // 默认渠道
                "default" // 默认租户
            );
        });
    }
    
    /**
     * 创建查询转换器管线
     */
    private QueryTransformerPipeline createQueryTransformerPipeline(ChatModel chatModel, 
                                                                   RagComponentConfig.QueryTransformerConfig config) {
        log.debug("构建查询转换器管线: config={}", config);
        
        // 创建管线配置
        QueryContext.PipelineConfig.PipelineConfigBuilder pipelineConfigBuilder = QueryContext.PipelineConfig.builder()
                .enableNormalization(config.isEnableNormalization())
                .enableExpanding(config.isEnableExpanding())
                .enableIntentRecognition(config.isIntentRecognitionEnabled())
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
        
        // 设置标准化配置
        if (config.getNormalizationConfig() != null) {
            QueryContext.NormalizationConfig normalizationConfig = createNormalizationConfig(
                    config.getNormalizationConfigOrDefault());
            pipelineConfigBuilder.normalizationConfig(normalizationConfig);
        }
        
        // 设置扩展配置
        if (config.getExpandingConfig() != null) {
            QueryContext.ExpandingConfig expandingConfig = createExpandingConfig(
                    config.getExpandingConfigOrDefault(), config);
            pipelineConfigBuilder.expandingConfig(expandingConfig);
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
     */
    private List<QueryTransformerStage> createPipelineStages(ChatModel chatModel, 
                                                           QueryContext.PipelineConfig config) {
        List<QueryTransformerStage> stages = new ArrayList<>();
        
        // 添加标准化阶段
        if (config.isEnableNormalization()) {
            stages.add(new NormalizationStage());
        }
        
        // 添加扩展阶段
        if (config.isEnableExpanding()) {
            stages.add(new ExpandingStage(chatModel));
        }
        
        log.debug("创建管线处理阶段完成: stageCount={}", stages.size());
        
        return stages;
    }
    
    /**
     * 创建标准化配置
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
     */
    public QueryRouter createQueryRouter(Long modelId) {
        return createQueryRouter(modelId, null, null, null, null);
    }

    /**
     * 根据模型ID和配置创建QueryRouter
     * 
     * @param modelId 模型ID
     * @param config 查询路由器配置，如果为null则使用默认配置
     */
    public QueryRouter createQueryRouter(Long modelId, RagComponentConfig.QueryRouterConfig config, RagComponentConfig.KnowledgeSearchConfig knowledgeSearchConfig, RagComponentConfig.WebSearchConfig webSearchConfig, RagComponentConfig.SqlQueryConfig sqlQueryConfig) {
        if (config != null) {
            log.debug("创建自定义配置的QueryRouter实例: modelId={}, config={}", modelId, config);
            // 使用组件级模型ID，未指定时回退到会话级 modelId
            Long actualModelId = config.getModelId() != null ? config.getModelId() : modelId;
            ChatModel chatModel = getChatModel(actualModelId);
            
            // 根据配置创建内容检索器
            Map<ContentRetriever, String> retrievers = new java.util.HashMap<>();
            
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
                        .embeddingModel(getEmbeddingModel(knowledgeModelId))
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
            ChatModel chatModel = getChatModel(id);
            
            // 创建内容检索器
            Map<ContentRetriever, String> retrievers = new java.util.HashMap<>();
            
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
     */
    public ReRankingContentAggregator createContentAggregator(Long modelId) {
        return createContentAggregator(modelId, null);
    }

    /**
     * 根据模型ID和配置创建ReRankingContentAggregator
     * 
     * @param modelId 模型ID
     * @param config 内容聚合器配置，如果为null则使用默认配置
     */
    public ReRankingContentAggregator createContentAggregator(Long modelId, RagComponentConfig.ContentAggregatorConfig config) {
        if (config != null) {
            log.debug("创建自定义配置的ReRankingContentAggregator实例: modelId={}, config={}", modelId, config);
            
            // 尝试设置评分模型，如果获取失败则使用无评分模型的配置
            try {
                ScoringModel scoringModel = null;
                if (config.getScoringModelId() != null) {
                    scoringModel = getScoringModel(config.getScoringModelId());
                } else {
                    scoringModel = getScoringModel(modelId);
                }
                
                if (scoringModel != null) {
                    return ReRankingContentAggregator.builder()
                            .maxResults(config.getMaxResults())
                            .minScore(config.getMinScore())
                            .scoringModel(scoringModel)
                            .build();
                } else {
                    log.warn("ScoringModel不可用，将使用基础的ContentAggregator");
                    return ReRankingContentAggregator.builder()
                            .maxResults(config.getMaxResults())
                            .minScore(config.getMinScore())
                            .build();
                }
            } catch (Exception e) {
                log.warn("获取ScoringModel失败，将使用基础的ContentAggregator: {}", e.getMessage());
                return ReRankingContentAggregator.builder()
                        .maxResults(config.getMaxResults())
                        .minScore(config.getMinScore())
                        .build();
            }
        }
        
        return contentAggregatorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建默认配置的ReRankingContentAggregator实例: modelId={}", id);
            return ReRankingContentAggregator.builder()
                    .maxResults(5)
                    .minScore(0.5)
                    .build();
        });
    }

    /**
     * 创建Web内容检索器（使用默认配置）
     */
    private ContentRetriever createWebContentRetriever() {
        return createWebContentRetriever(null);
    }

    /**
     * 创建Web内容检索器
     * 
     * @param config Web搜索配置，如果为null则使用默认配置
     * @return ContentRetriever实例，如果全局开关禁用则返回null
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
     */
    private ContentRetriever createSqlQueryContentRetriever(Long chatModelId, Long embeddingModelId, RagComponentConfig.SqlQueryConfig config) {
        return new SqlQueryContentRetriever(jdbcTemplate, nlpToSqlService, chatModelId, embeddingModelId);
    }

    /**
     * 创建智能聊天服务（使用默认RAG配置）
     * 
     * <p>使用系统默认的RAG组件配置创建智能聊天服务实例。</p>
     * 
     * <h3>默认配置:</h3>
     * <ul>
     *   <li>内容聚合器：maxResults=5, minScore=0.5</li>
     *   <li>查询转换器：n=5</li>
     *   <li>Web搜索：maxResults=10</li>
     *   <li>知识库搜索：使用EmbeddingStore默认配置</li>
     * </ul>
     * 
     * @param modelId 模型ID，必须是有效的模型标识
     * @return SmartChatService实例，包含完整的RAG能力
     * @throws RuntimeException 当模型不存在或创建服务失败时
     */
    public SmartChatService createSmartChatService(Long modelId) {
        return createSmartChatService(modelId, null);
    }

    /**
     * 创建智能聊天服务（支持自定义RAG配置）
     * 
     * <p>根据提供的RAG配置创建智能聊天服务实例。支持前端动态自定义各个RAG组件的参数，
     * 以满足不同场景下的检索和生成需求。</p>
     * 
     * <h3>配置参数说明:</h3>
     * <ul>
     *   <li><strong>内容聚合器</strong>：控制最终返回给LLM的内容数量和质量阈值</li>
     *   <li><strong>查询转换器</strong>：控制将用户查询扩展为多个变体的数量</li>
     *   <li><strong>查询路由器</strong>：控制启用哪些检索器（知识库、Web搜索、SQL查询）</li>
     *   <li><strong>Web搜索</strong>：控制Web搜索的结果数量和超时设置</li>
     *   <li><strong>知识库搜索</strong>：控制向量搜索的topK和相关性阈值</li>
     * </ul>
     * 
     * <h3>性能考虑:</h3>
     * <ul>
     *   <li>使用自定义配置的服务实例不会被缓存</li>
     *   <li>每次调用都会创建新的RAG组件实例</li>
     *   <li>建议在需要定制化配置时使用，否则使用默认配置版本</li>
     * </ul>
     * 
     * @param modelId 模型ID，必须是有效的模型标识
     * @param ragConfig RAG组件配置，如果为null则使用系统默认配置
     * @return SmartChatService实例，包含根据配置定制的RAG能力
     * @throws RuntimeException 当模型不存在或创建服务失败时
     * @see RagComponentConfig
     */
    public SmartChatService createSmartChatService(Long modelId, RagComponentConfig ragConfig) {
        log.info("创建SmartChatService: modelId={}, ragConfig={}", modelId, ragConfig);
        
        try {
            ChatModel chatModel = getChatModel(modelId);
            StreamingChatModel streamingChatModel = getStreamingChatModel(modelId);
            RetrievalAugmentor retrievalAugmentor = createRetrievalAugmentor(modelId, ragConfig);
            
            return AiServices.builder(SmartChatService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .retrievalAugmentor(retrievalAugmentor)
                    .build();
            
        } catch (Exception e) {
            log.error("创建SmartChatService失败: modelId={}, ragConfig={}", modelId, ragConfig, e);
            throw new RuntimeException("无法创建SmartChatService: " + e.getMessage(), e);
        }
    }

    /**
     * 创建结构化聊天服务
     * 
     * @param modelId 模型ID
     * @return StructuredChatServiceAi实例
     */
    public StructuredChatServiceAi createStructuredChatService(Long modelId) {
        log.info("创建StructuredChatService: modelId={}", modelId);
        
        try {
            ChatModel chatModel = getChatModel(modelId);
            StreamingChatModel streamingChatModel = getStreamingChatModel(modelId);
            RetrievalAugmentor retrievalAugmentor = createRetrievalAugmentor(modelId);
            
            return AiServices.builder(StructuredChatServiceAi.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .retrievalAugmentor(retrievalAugmentor)
                    .build();
            
        } catch (Exception e) {
            log.error("创建StructuredChatService失败: modelId={}", modelId, e);
            throw new RuntimeException("无法创建StructuredChatService: " + e.getMessage(), e);
        }
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

    /**
     * 扩展的缓存统计信息，包含所有缓存
     * 
     * @return 完整的缓存统计信息
     */
    public Map<String, Integer> getAllCacheStats() {
        Map<String, Integer> stats = getCacheStats();
        stats.putAll(getRagComponentCacheStats());
        return stats;
    }
}
