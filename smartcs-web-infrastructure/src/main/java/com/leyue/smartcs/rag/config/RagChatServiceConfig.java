package com.leyue.smartcs.rag.config;

import com.leyue.smartcs.app.service.SmartChatService;
import com.leyue.smartcs.app.service.StructuredChatServiceAi;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.model.ai.ModelInferenceService;
import com.leyue.smartcs.rag.retriever.EnhancedContentAggregator;
import com.leyue.smartcs.rag.retriever.EnhancedContentInjector;
import com.leyue.smartcs.rag.retriever.KnowledgeContentRetriever;
import com.leyue.smartcs.rag.tools.KnowledgeSearchTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    private final KnowledgeContentRetriever retriever;
    private final EnhancedContentInjector injector;
    private final KnowledgeSearchTool knowledgeSearchTool;
    private final DynamicModelManager dynamicModelManager;

    /**
     * 创建RAG增强器
     */
    @Bean
    public RetrievalAugmentor retrievalAugmentor() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .contentAggregator(aggregator())
                .contentInjector(injector)
                .build();
    }

    @Bean
    public ReRankingContentAggregator aggregator() {
        return ReRankingContentAggregator.builder()
                .maxResults(5)
                .minScore(0.5)
                .scoringModel()
                .querySelector(aggregator);
    }

    /**
     * 创建智能聊天服务
     * 完全基于LangChain4j AI Services框架，声明式配置
     */
    @Bean
    public SmartChatService smartChatService() {
        log.info("创建智能聊天服务 - 基于LangChain4j AI Services");

        return AiServices.builder(SmartChatService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .retrievalAugmentor(retrievalAugmentor())
                .tools(knowledgeSearchTool) // 注入知识库搜索工具
                .build();
    }

    /**
     * 创建结构化聊天服务 - 基于AiServices框架
     * 自动集成RAG、记忆管理和结构化输出
     */
    @Bean
    public StructuredChatServiceAi structuredChatServiceAi() {
        log.info("创建结构化聊天服务 - 基于LangChain4j AiServices");

        return AiServices.builder(StructuredChatServiceAi.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .retrievalAugmentor(retrievalAugmentor())
                .build();
    }

    /**
     * 配置ModelInferenceService - 使用默认模型
     * 注意：这是一个简化版本，实际使用时需要支持动态模型切换
     */
    @Bean("modelInferenceService")
    public ModelInferenceService modelInferenceService() {
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
                    .retrievalAugmentor(retrievalAugmentor())
                    .build();

        } catch (Exception e) {
            log.error("创建ModelInferenceService失败: defaultModelId={}", defaultModelId, e);
            throw new RuntimeException("无法创建ModelInferenceService", e);
        }
    }
}