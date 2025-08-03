package com.leyue.smartcs.app.config;

import com.leyue.smartcs.app.service.StructuredChatServiceAi;
import com.leyue.smartcs.rag.retriever.KnowledgeContentRetriever;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 结构化聊天服务配置 - 基于AiServices框架
 * 声明式配置StructuredChatServiceAi，自动集成RAG和记忆管理
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class StructuredChatServiceConfig {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryStore chatMemoryStore;
    private final KnowledgeContentRetriever knowledgeContentRetriever;

    /**
     * 创建RAG增强器 - 用于知识库检索
     */
    @Bean("structuredChatRetrievalAugmentor")
    public RetrievalAugmentor structuredChatRetrievalAugmentor() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(knowledgeContentRetriever)
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
                .retrievalAugmentor(structuredChatRetrievalAugmentor())
                .build();
    }
} 