package com.leyue.smartcs.rag.config;

import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.model.ai.ModelInferenceService;
import com.leyue.smartcs.rag.SmartChatService;
import com.leyue.smartcs.rag.content.retriever.SqlQueryContentRetriever;
import com.leyue.smartcs.rag.StructuredChatServiceAi;
import dev.langchain4j.community.web.search.searxng.SearXNGWebSearchEngine;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 智能聊天服务配置
 * 声明式配置SmartChatService，完全基于LangChain4j框架
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RagChatServiceConfig {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryStore chatMemoryStore;
    private final DynamicModelManager dynamicModelManager;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 创建RAG增强器
     */
    @Bean
    public RetrievalAugmentor retrievalAugmentor(QueryRouter queryRouter, 
                                                QueryTransformer queryTransformer,
                                                ReRankingContentAggregator contentAggregator,
                                                ContentInjector contentInjector) {
        return DefaultRetrievalAugmentor.builder()
//                .contentRetriever(contentRetriever())
                .queryRouter(queryRouter)
                .queryTransformer(queryTransformer)
                .contentAggregator(contentAggregator)
                .contentInjector(contentInjector)
                .build();
    }

    @Bean
    public ContentInjector contentInjector() {
        return DefaultContentInjector.builder()
                .promptTemplate(null)
                .metadataKeysToInclude( null)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever() {
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    @Bean
    public ContentRetriever webContentRetriever(SearXNGWebSearchEngine searxngWebSearchEngine) {
        return WebSearchContentRetriever.builder()
                .webSearchEngine(searxngWebSearchEngine)
                .maxResults(10)
                .build();
    }

    @Bean
    public ContentRetriever sqlQueryContentRetriever() {
        return new SqlQueryContentRetriever(null); // JdbcTemplate将通过构造函数注入
    }
    @Bean
    public QueryTransformer queryTransformer() {
        return ExpandingQueryTransformer.builder()
                .chatModel(chatModel)
                .n(5)
                .promptTemplate(null)
                .build();
    }

    @Bean
    public QueryRouter queryRouter(ContentRetriever contentRetriever, 
                                  ContentRetriever webContentRetriever, 
                                  ContentRetriever sqlQueryContentRetriever) {
        return LanguageModelQueryRouter.builder()
                .chatModel(chatModel)
                .promptTemplate(null)
                .retrieverToDescription(Map.of(contentRetriever, "知识库检索", webContentRetriever, "Web搜索", sqlQueryContentRetriever, "数据库查询"))
                .build();
    }

    /**
     * 创建重排序内容聚合器
     * 基于LangChain4j 1.1.0最佳实践，使用简化配置避免过度复杂化
     */
    @Bean
    public ReRankingContentAggregator contentAggregator() {
        // 使用LangChain4j推荐的默认重排序配置
        // 对于生产环境，可以根据需要配置专用的ScoringModel
        return ReRankingContentAggregator.builder()
                .maxResults(5)
                .minScore(0.5)
                // 暂时不配置scoringModel，使用默认的基于相似度的排序
                .build();
    }

    /**
     * 创建智能聊天服务
     * 完全基于LangChain4j AI Services框架，声明式配置
     */
    @Bean
    public SmartChatService smartChatService(RetrievalAugmentor retrievalAugmentor) {
        log.info("创建智能聊天服务 - 基于LangChain4j AI Services");

        return AiServices.builder(SmartChatService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder() //记忆
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore) // 持久化
                        .build())
                .retrievalAugmentor(retrievalAugmentor)
//                .tools(knowledgeSearchTool) // 注入知识库搜索工具
                .build();
    }

    /**
     * 创建结构化聊天服务 - 基于AiServices框架
     * 自动集成RAG、记忆管理和结构化输出
     */
    @Bean
    public StructuredChatServiceAi structuredChatServiceAi(RetrievalAugmentor retrievalAugmentor) {
        log.info("创建结构化聊天服务 - 基于LangChain4j AiServices");

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
    }

    /**
     * 配置ModelInferenceService - 使用默认模型
     * 注意：这是一个简化版本，实际使用时需要支持动态模型切换
     */
    @Bean("modelInferenceService")
    public ModelInferenceService modelInferenceService(RetrievalAugmentor retrievalAugmentor) {
        Long defaultModelId = 1L; // 默认模型ID，实际应用中应该从配置中获取

        log.info("创建ModelInferenceService: defaultModelId={}", defaultModelId);

        try {
            return AiServices.builder(ModelInferenceService.class)
                    .chatModel(dynamicModelManager.getChatModel(defaultModelId))
                    .streamingChatModel(dynamicModelManager.getStreamingChatModel(defaultModelId))
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .retrievalAugmentor(retrievalAugmentor)
                    .build();

        } catch (Exception e) {
            log.error("创建ModelInferenceService失败: defaultModelId={}", defaultModelId, e);
            throw new RuntimeException("无法创建ModelInferenceService", e);
        }
    }
}