package com.leyue.smartcs.model.ai;

import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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
    private final ModelBeanManagerService modelBeanManagerService;
    
    // 缓存模型实例，避免重复创建
    private final Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();

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
    }

    /**
     * 清除所有模型缓存
     */
    public void clearAllCache() {
        log.info("清除所有模型缓存");
        chatModelCache.clear();
        streamingChatModelCache.clear();
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
        Object modelBean = modelBeanManagerService.getModelBean(provider, "chat");
        if (modelBean instanceof ChatModel) {
            return (ChatModel) modelBean;
        }
        throw new IllegalStateException("无法获取ChatModel实例: provider=" + provider.getProviderType().getKey());
    }

    /**
     * 构建StreamingChatModel实例
     */
    private StreamingChatModel buildStreamingChatModel(Provider provider) {
        Object modelBean = modelBeanManagerService.getModelBean(provider, "streaming_chat");
        if (modelBean instanceof StreamingChatModel) {
            return (StreamingChatModel) modelBean;
        }
        throw new IllegalStateException("无法获取StreamingChatModel实例: provider=" + provider.getProviderType().getKey());
    }
}