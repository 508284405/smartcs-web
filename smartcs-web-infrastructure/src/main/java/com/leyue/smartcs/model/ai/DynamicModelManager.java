package com.leyue.smartcs.model.ai;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
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
    
    // 缓存模型实例，避免重复创建
    private final Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();

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
     * 创建模型推理服务
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
            
            // 使用LangChain4j AiServices框架创建推理服务
            // 注意：这里需要从Spring上下文获取RAG相关的配置
            return createAiServiceWithRag(chatModel, streamingChatModel, knowledgeIds);
            
        } catch (Exception e) {
            log.error("创建ModelInferenceService失败: modelId={}, knowledgeIds={}", modelId, knowledgeIds, e);
            throw new RuntimeException("无法创建ModelInferenceService: " + e.getMessage(), e);
        }
    }

    /**
     * 创建带RAG增强的AI服务
     * 使用简化的RAG配置，避免循环依赖问题
     */
    private ModelInferenceService createAiServiceWithRag(ChatModel chatModel, 
                                                        StreamingChatModel streamingChatModel, 
                                                        java.util.List<Long> knowledgeIds) {
        // 简化版本：直接创建不带RAG的服务
        // 生产环境中可以根据knowledgeIds动态配置RAG增强器
        return dev.langchain4j.service.AiServices.builder(ModelInferenceService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                // 暂时不添加RAG增强器，避免循环依赖
                // .retrievalAugmentor(retrievalAugmentor) 
                .build();
    }
}