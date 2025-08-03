package com.leyue.smartcs.app.config;

import com.leyue.smartcs.app.rag.KnowledgeContentRetriever;
import com.leyue.smartcs.app.service.SmartChatService;
import com.leyue.smartcs.app.tools.KnowledgeSearchTool;
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
 * 智能聊天服务配置
 * 声明式配置SmartChatService，完全基于LangChain4j框架
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SmartChatServiceConfig {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryStore chatMemoryStore;
    private final KnowledgeContentRetriever knowledgeContentRetriever;
    private final KnowledgeSearchTool knowledgeSearchTool;

    /**
     * 创建RAG增强器
     */
    @Bean
    public RetrievalAugmentor retrievalAugmentor() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(knowledgeContentRetriever)
                .build();
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
}