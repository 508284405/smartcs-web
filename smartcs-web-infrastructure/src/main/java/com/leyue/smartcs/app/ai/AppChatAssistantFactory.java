package com.leyue.smartcs.app.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * AI应用聊天助手工厂
 * 支持动态模型切换和多实例管理，现在使用增强版助手
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppChatAssistantFactory {

    private final ApplicationContext applicationContext;
    
    // 缓存不同模型的助手实例
    private final Map<String, AppChatAssistant> assistantCache = new ConcurrentHashMap<>();

    /**
     * 获取或创建AI聊天助手
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     * @return AI聊天助手
     */
    public AppChatAssistant getOrCreateAssistant(ChatModel chatModel, 
                                               StreamingChatModel streamingChatModel,
                                               String modelKey) {
        return assistantCache.computeIfAbsent(modelKey, key -> {
            log.info("创建新的增强版AI聊天助手实例: modelKey={}", key);
            
            // 创建增强版助手实例
            EnhancedAppChatAssistant enhancedAssistant = applicationContext.getBean(EnhancedAppChatAssistant.class);
            enhancedAssistant.initialize(chatModel, streamingChatModel, modelKey);
            
            return enhancedAssistant;
        });
    }

    /**
     * 获取或创建传统AI聊天助手（兼容性方法）
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     * @return 传统AI聊天助手
     */
    public AppChatAssistant getOrCreateLegacyAssistant(dev.langchain4j.model.chat.ChatModel chatModel, 
                                                     dev.langchain4j.model.chat.StreamingChatModel streamingChatModel,
                                                     String modelKey) {
        String legacyKey = "legacy_" + modelKey;
        return assistantCache.computeIfAbsent(legacyKey, key -> {
            log.info("创建传统AI聊天助手实例: modelKey={}", key);
            
            // 使用传统实现作为后备
            return new AppChatAssistantImpl(chatModel, streamingChatModel, 
                applicationContext.getBean(dev.langchain4j.store.memory.chat.ChatMemoryStore.class));
        });
    }

    /**
     * 清除指定模型的助手缓存
     * @param modelKey 模型唯一标识
     */
    public void clearAssistantCache(String modelKey) {
        log.info("清除AI聊天助手缓存: modelKey={}", modelKey);
        assistantCache.remove(modelKey);
        assistantCache.remove("legacy_" + modelKey); // 同时清除传统版本
    }

    /**
     * 清除所有助手缓存
     */
    public void clearAllCache() {
        log.info("清除所有AI聊天助手缓存");
        assistantCache.clear();
    }

    /**
     * 获取缓存大小
     * @return 缓存实例数量
     */
    public int getCacheSize() {
        return assistantCache.size();
    }

    /**
     * 预热助手缓存
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     */
    public void warmupAssistants(ChatModel chatModel, 
                               StreamingChatModel streamingChatModel,
                               String modelKey) {
        log.info("预热AI聊天助手缓存: modelKey={}", modelKey);
        getOrCreateAssistant(chatModel, streamingChatModel, modelKey);
        log.info("AI聊天助手缓存预热完成: modelKey={}", modelKey);
    }
}