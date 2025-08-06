package com.leyue.smartcs.model.ai;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.rag.SmartChatService;
import com.leyue.smartcs.rag.StructuredChatServiceAi;
import com.leyue.smartcs.rag.content.retriever.SqlQueryContentRetriever;
import dev.langchain4j.community.web.search.searxng.SearXNGWebSearchEngine;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
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
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

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
    private final ChatMemoryStore chatMemoryStore;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final SearXNGWebSearchEngine searxngWebSearchEngine;
    
    // 缓存模型实例，避免重复创建
    private final Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
    
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
            return buildChatModel(provider);
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
            return buildStreamingChatModel(provider);
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
            return buildEmbeddingModel(provider);
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
    }

    /**
     * 清除所有模型缓存
     */
    public void clearAllCache() {
        log.info("清除所有模型缓存");
        chatModelCache.clear();
        streamingChatModelCache.clear();
        embeddingModelCache.clear();
        
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
     */
    private ChatModel buildChatModel(Provider provider) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            return OpenAiChatModel.builder()
                    .baseUrl(provider.getEndpoint())
                    .apiKey(provider.getApiKey())
                    .build();
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建StreamingChatModel实例
     */
    private StreamingChatModel buildStreamingChatModel(Provider provider) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            return OpenAiStreamingChatModel.builder()
                    .baseUrl(provider.getEndpoint())
                    .apiKey(provider.getApiKey())
                    .build();
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建EmbeddingModel实例
     */
    private EmbeddingModel buildEmbeddingModel(Provider provider) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            return OpenAiEmbeddingModel.builder()
                    .baseUrl(provider.getEndpoint())
                    .apiKey(provider.getApiKey())
                    .build();
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
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
     * 根据模型ID创建RetrievalAugmentor
     */
    public RetrievalAugmentor createRetrievalAugmentor(Long modelId) {
        return retrievalAugmentorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建RetrievalAugmentor实例: modelId={}", id);
            
            return DefaultRetrievalAugmentor.builder()
                    .queryRouter(createQueryRouter(id))
                    .queryTransformer(createQueryTransformer(id))
                    .contentAggregator(createContentAggregator(id))
                    .contentInjector(createContentInjector(id))
                    .build();
        });
    }

    /**
     * 根据模型ID创建ContentInjector
     */
    public ContentInjector createContentInjector(Long modelId) {
        return contentInjectorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建ContentInjector实例: modelId={}", id);
            return DefaultContentInjector.builder()
                    .promptTemplate(null)
                    .metadataKeysToInclude(null)
                    .build();
        });
    }

    /**
     * 根据模型ID创建QueryTransformer
     */
    public QueryTransformer createQueryTransformer(Long modelId) {
        return queryTransformerCache.computeIfAbsent(modelId, id -> {
            log.debug("创建QueryTransformer实例: modelId={}", id);
            ChatModel chatModel = getChatModel(id);
            return ExpandingQueryTransformer.builder()
                    .chatModel(chatModel)
                    .n(5)
                    .promptTemplate(null)
                    .build();
        });
    }

    /**
     * 根据模型ID创建QueryRouter
     */
    public QueryRouter createQueryRouter(Long modelId) {
        return queryRouterCache.computeIfAbsent(modelId, id -> {
            log.debug("创建QueryRouter实例: modelId={}", id);
            ChatModel chatModel = getChatModel(id);
            
            // 创建内容检索器
            ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.from(embeddingStore);
            ContentRetriever webContentRetriever = createWebContentRetriever();
            ContentRetriever sqlQueryContentRetriever = createSqlQueryContentRetriever();
            
            return LanguageModelQueryRouter.builder()
                    .chatModel(chatModel)
                    .promptTemplate(null)
                    .retrieverToDescription(Map.of(
                        contentRetriever, "知识库检索", 
                        webContentRetriever, "Web搜索", 
                        sqlQueryContentRetriever, "数据库查询"))
                    .build();
        });
    }

    /**
     * 根据模型ID创建ReRankingContentAggregator
     */
    public ReRankingContentAggregator createContentAggregator(Long modelId) {
        return contentAggregatorCache.computeIfAbsent(modelId, id -> {
            log.debug("创建ReRankingContentAggregator实例: modelId={}", id);
            return ReRankingContentAggregator.builder()
                    .maxResults(5)
                    .minScore(0.5)
                    .build();
        });
    }

    /**
     * 创建Web内容检索器
     */
    private ContentRetriever createWebContentRetriever() {
        return WebSearchContentRetriever.builder()
                .webSearchEngine(searxngWebSearchEngine)
                .maxResults(10)
                .build();
    }

    /**
     * 创建SQL查询内容检索器
     */
    private ContentRetriever createSqlQueryContentRetriever() {
        return new SqlQueryContentRetriever(null); // JdbcTemplate将通过构造函数注入
    }

    /**
     * 创建智能聊天服务
     * 
     * @param modelId 模型ID
     * @return SmartChatService实例
     */
    public SmartChatService createSmartChatService(Long modelId) {
        log.info("创建SmartChatService: modelId={}", modelId);
        
        try {
            ChatModel chatModel = getChatModel(modelId);
            StreamingChatModel streamingChatModel = getStreamingChatModel(modelId);
            RetrievalAugmentor retrievalAugmentor = createRetrievalAugmentor(modelId);
            
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
            log.error("创建SmartChatService失败: modelId={}", modelId, e);
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