package com.leyue.smartcs.model.config;

import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.model.ai.ModelInferenceService;
import com.leyue.smartcs.model.rag.ModelKnowledgeRetriever;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模型推理服务配置类
 * 基于LangChain4j框架配置声明式AI服务
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ModelInferenceServiceConfig {

    private final DynamicModelManager dynamicModelManager;
    private final ModelKnowledgeRetriever modelKnowledgeRetriever;
    private final ChatMemoryStore chatMemoryStore;

    /**
     * 配置RetrievalAugmentor用于RAG增强
     */
    @Bean("modelInferenceRetrievalAugmentor")
    public RetrievalAugmentor modelInferenceRetrievalAugmentor() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(modelKnowledgeRetriever)
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
                    .retrievalAugmentor(modelInferenceRetrievalAugmentor())
                    .build();

        } catch (Exception e) {
            log.error("创建ModelInferenceService失败: defaultModelId={}", defaultModelId, e);
            throw new RuntimeException("无法创建ModelInferenceService", e);
        }
    }

    /**
     * 创建特定模型的ModelInferenceService
     * 这个方法可以在运行时动态调用来创建不同模型的服务实例
     * 
     * @param modelId 模型ID
     * @return ModelInferenceService实例
     */
    public ModelInferenceService createModelInferenceService(Long modelId) {
        log.info("创建特定模型的ModelInferenceService: modelId={}", modelId);

        try {
            // 检查模型是否支持推理
            if (!dynamicModelManager.supportsInference(modelId)) {
                throw new IllegalArgumentException("模型不支持推理: modelId=" + modelId);
            }

            return AiServices.builder(ModelInferenceService.class)
                    .chatModel(dynamicModelManager.getChatModel(modelId))
                    .streamingChatModel(dynamicModelManager.getStreamingChatModel(modelId))
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .retrievalAugmentor(modelInferenceRetrievalAugmentor())
                    .build();

        } catch (Exception e) {
            log.error("创建特定模型的ModelInferenceService失败: modelId={}", modelId, e);
            throw new RuntimeException("无法创建ModelInferenceService: modelId=" + modelId, e);
        }
    }

    /**
     * 创建无RAG增强的ModelInferenceService
     * 用于不需要知识库检索的场景
     * 
     * @param modelId 模型ID
     * @return ModelInferenceService实例
     */
    public ModelInferenceService createSimpleModelInferenceService(Long modelId) {
        log.info("创建无RAG增强的ModelInferenceService: modelId={}", modelId);

        try {
            // 检查模型是否支持推理
            if (!dynamicModelManager.supportsInference(modelId)) {
                throw new IllegalArgumentException("模型不支持推理: modelId=" + modelId);
            }

            return AiServices.builder(ModelInferenceService.class)
                    .chatModel(dynamicModelManager.getChatModel(modelId))
                    .streamingChatModel(dynamicModelManager.getStreamingChatModel(modelId))
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    // 注意：没有设置retrievalAugmentor，因此不会进行RAG增强
                    .build();

        } catch (Exception e) {
            log.error("创建简单ModelInferenceService失败: modelId={}", modelId, e);
            throw new RuntimeException("无法创建简单ModelInferenceService: modelId=" + modelId, e);
        }
    }

    /**
     * 验证配置是否正确
     * 
     * @return 配置是否有效
     */
    public boolean validateConfiguration() {
        try {
            // 检查关键组件是否可用
            if (dynamicModelManager == null) {
                log.error("DynamicModelManager未配置");
                return false;
            }

            if (modelKnowledgeRetriever == null) {
                log.error("ModelKnowledgeRetriever未配置");
                return false;
            }

            if (chatMemoryStore == null) {
                log.error("ChatMemoryStore未配置");
                return false;
            }

            log.info("ModelInferenceServiceConfig配置验证通过");
            return true;

        } catch (Exception e) {
            log.error("配置验证失败", e);
            return false;
        }
    }
}