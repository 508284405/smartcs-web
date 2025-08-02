package com.leyue.smartcs.app.service;

import com.leyue.smartcs.app.rag.RagOrchestrator;
import com.leyue.smartcs.app.tools.ToolManager;
import com.leyue.smartcs.domain.app.service.AiAppChatService;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

/**
 * AI应用聊天服务工厂
 * 基于LangChain4j AI Services创建聊天服务实例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiAppChatServiceFactory {

    private final ChatMemoryStore chatMemoryStore;
    private final RagOrchestrator ragOrchestrator;
    private final ToolManager toolManager;
    
    // 配置参数
    private static final int MAX_CACHE_SIZE = 100; // 最大缓存实例数
    private static final long CACHE_CLEANUP_INTERVAL_MINUTES = 30; // 清理间隔
    private static final long SERVICE_IDLE_TIMEOUT_MINUTES = 60; // 服务空闲超时
    
    // 使用LRU缓存替代普通Map，防止内存泄漏
    private final Map<String, CachedService> serviceCache = Collections.synchronizedMap(
        new LinkedHashMap<String, CachedService>(MAX_CACHE_SIZE + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedService> eldest) {
                if (size() > MAX_CACHE_SIZE) {
                    log.info("LRU驱逐缓存实例: key={}", eldest.getKey());
                    return true;
                }
                return false;
            }
        }
    );
    
    // 定期清理调度器
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread t = new Thread(r, "AiChatService-Cleanup");
            t.setDaemon(true);
            return t;
        });
    
    // 缓存服务包装类
    private static class CachedService {
        final AiAppChatService service;
        volatile long lastAccessTime;
        
        CachedService(AiAppChatService service) {
            this.service = service;
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        boolean isExpired(long timeoutMillis) {
            return System.currentTimeMillis() - lastAccessTime > timeoutMillis;
        }
    }

    /**
     * 创建或获取AI聊天服务实例
     * 
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     * @param enableRag 是否启用RAG
     * @return AI聊天服务实例
     */
    public AiAppChatService getOrCreateService(ChatModel chatModel,
                                             StreamingChatModel streamingChatModel,
                                             String modelKey,
                                             boolean enableRag) {
        return getOrCreateService(chatModel, streamingChatModel, modelKey, enableRag, false, null);
    }
    
    /**
     * 创建或获取AI聊天服务实例（完整版本）
     * 
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     * @param enableRag 是否启用RAG
     * @param enableTools 是否启用工具
     * @param knowledgeBaseId 知识库ID（用于工具）
     * @return AI聊天服务实例
     */
    public AiAppChatService getOrCreateService(ChatModel chatModel,
                                             StreamingChatModel streamingChatModel,
                                             String modelKey,
                                             boolean enableRag,
                                             boolean enableTools,
                                             Long knowledgeBaseId) {
        String cacheKey = modelKey + "_" + enableRag + "_" + enableTools + "_" + knowledgeBaseId;
        
        // 启动清理调度器（如果尚未启动）
        initializeCleanupScheduler();
        
        CachedService cachedService = serviceCache.computeIfAbsent(cacheKey, key -> {
            log.info("创建新的AI聊天服务实例: modelKey={}, enableRag={}, enableTools={}, knowledgeBaseId={}", 
                    modelKey, enableRag, enableTools, knowledgeBaseId);
            
            AiServices.Builder<AiAppChatService> builder = AiServices.builder(AiAppChatService.class)
                    .chatModel(chatModel)
                    .chatMemoryProvider(memoryId -> createChatMemory(memoryId.toString()));

            // 如果有流式模型，设置流式模型
            if (streamingChatModel != null) {
                builder.streamingChatModel(streamingChatModel);
            }

            // 如果启用RAG，配置检索增强器
            if (enableRag) {
                RetrievalAugmentor retrievalAugmentor = createRetrievalAugmentor();
                builder.retrievalAugmentor(retrievalAugmentor);
            }
            
            // 如果启用工具，配置工具
            if (enableTools) {
                if (knowledgeBaseId != null) {
                    // 为特定知识库配置工具
                    builder.tools(toolManager.getToolsForKnowledgeBase(knowledgeBaseId));
                } else {
                    // 配置所有可用工具
                    builder.tools(toolManager.getAllTools());
                }
            }

            return new CachedService(builder.build());
        });
        
        // 更新访问时间
        cachedService.updateAccessTime();
        return cachedService.service;
    }

    /**
     * 创建专用于RAG的聊天服务
     * 
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     * @return 支持RAG的聊天服务实例
     */
    public AiAppChatService createRagService(ChatModel chatModel,
                                           StreamingChatModel streamingChatModel,
                                           String modelKey) {
        log.info("创建RAG聊天服务实例: modelKey={}", modelKey);

        AiServices.Builder<AiAppChatService> builder = AiServices.builder(AiAppChatService.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> createChatMemory(memoryId.toString()));

        // 设置流式模型
        if (streamingChatModel != null) {
            builder.streamingChatLanguageModel(streamingChatModel);
        }

        // 配置RAG检索增强器
        RetrievalAugmentor retrievalAugmentor = createRetrievalAugmentor();
        builder.retrievalAugmentor(retrievalAugmentor);

        return builder.build();
    }

    /**
     * 创建不带RAG的简单聊天服务
     * 
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     * @return 简单聊天服务实例
     */
    public AiAppChatService createSimpleService(ChatModel chatModel,
                                              StreamingChatModel streamingChatModel,
                                              String modelKey) {
        log.info("创建简单聊天服务实例: modelKey={}", modelKey);

        AiServices.Builder<AiAppChatService> builder = AiServices.builder(AiAppChatService.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> createChatMemory(memoryId.toString()));

        // 设置流式模型
        if (streamingChatModel != null) {
            builder.streamingChatLanguageModel(streamingChatModel);
        }

        return builder.build();
    }

    /**
     * 创建聊天记忆
     * 
     * @param memoryId 记忆ID
     * @return 聊天记忆实例
     */
    private ChatMemory createChatMemory(String memoryId) {
        return MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20) // 保留最近20条消息
                .chatMemoryStore(chatMemoryStore)
                .build();
    }

    /**
     * 创建检索增强器
     * 
     * @return 检索增强器实例
     */
    private RetrievalAugmentor createRetrievalAugmentor() {
        // 这里可以配置更复杂的检索增强器
        // 暂时使用默认实现
        return DefaultRetrievalAugmentor.builder()
                .build();
    }

    /**
     * 初始化清理调度器
     */
    private volatile boolean cleanupInitialized = false;
    
    private void initializeCleanupScheduler() {
        if (!cleanupInitialized) {
            synchronized (this) {
                if (!cleanupInitialized) {
                    cleanupScheduler.scheduleAtFixedRate(
                        this::cleanupExpiredServices,
                        CACHE_CLEANUP_INTERVAL_MINUTES,
                        CACHE_CLEANUP_INTERVAL_MINUTES,
                        TimeUnit.MINUTES
                    );
                    cleanupInitialized = true;
                    log.info("启动AI聊天服务缓存清理调度器: 间隔={}分钟", CACHE_CLEANUP_INTERVAL_MINUTES);
                }
            }
        }
    }
    
    /**
     * 清理过期的服务实例
     */
    private void cleanupExpiredServices() {
        try {
            long timeoutMillis = SERVICE_IDLE_TIMEOUT_MINUTES * 60 * 1000;
            int initialSize = serviceCache.size();
            
            serviceCache.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired(timeoutMillis)) {
                    log.debug("清理过期的AI服务实例: key={}", entry.getKey());
                    return true;
                }
                return false;
            });
            
            int finalSize = serviceCache.size();
            if (initialSize != finalSize) {
                log.info("清理完成: 清理前={}, 清理后={}, 清理数量={}", initialSize, finalSize, initialSize - finalSize);
            }
            
        } catch (Exception e) {
            log.error("清理过期服务实例失败", e);
        }
    }

    /**
     * 清除指定模型的服务缓存
     * 
     * @param modelKey 模型唯一标识
     */
    public void clearServiceCache(String modelKey) {
        log.info("清除AI聊天服务缓存: modelKey={}", modelKey);
        serviceCache.entrySet().removeIf(entry -> entry.getKey().startsWith(modelKey));
    }

    /**
     * 清除所有服务缓存
     */
    public void clearAllCache() {
        log.info("清除所有AI聊天服务缓存");
        serviceCache.clear();
    }

    /**
     * 获取缓存大小
     * 
     * @return 缓存实例数量
     */
    public int getCacheSize() {
        return serviceCache.size();
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    public CacheStats getCacheStats() {
        long now = System.currentTimeMillis();
        int totalSize = serviceCache.size();
        int expiredCount = 0;
        long oldestAccess = now;
        long newestAccess = 0;
        
        for (CachedService cachedService : serviceCache.values()) {
            long accessTime = cachedService.lastAccessTime;
            if (cachedService.isExpired(SERVICE_IDLE_TIMEOUT_MINUTES * 60 * 1000)) {
                expiredCount++;
            }
            if (accessTime < oldestAccess) {
                oldestAccess = accessTime;
            }
            if (accessTime > newestAccess) {
                newestAccess = accessTime;
            }
        }
        
        return new CacheStats(totalSize, expiredCount, oldestAccess, newestAccess);
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        public final int totalSize;
        public final int expiredCount;
        public final long oldestAccessTime;
        public final long newestAccessTime;
        
        public CacheStats(int totalSize, int expiredCount, long oldestAccessTime, long newestAccessTime) {
            this.totalSize = totalSize;
            this.expiredCount = expiredCount;
            this.oldestAccessTime = oldestAccessTime;
            this.newestAccessTime = newestAccessTime;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{total=%d, expired=%d, oldest=%d, newest=%d}", 
                               totalSize, expiredCount, oldestAccessTime, newestAccessTime);
        }
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        log.info("关闭AI聊天服务工厂资源");
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        serviceCache.clear();
    }

    /**
     * 预热服务缓存
     * 
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     */
    public void warmupServices(ChatModel chatModel,
                             StreamingChatModel streamingChatModel,
                             String modelKey) {
        log.info("预热AI聊天服务: modelKey={}", modelKey);
        
        // 预创建常用的服务实例
        getOrCreateService(chatModel, streamingChatModel, modelKey, false);
        getOrCreateService(chatModel, streamingChatModel, modelKey, true);
        
        log.info("AI聊天服务预热完成: modelKey={}", modelKey);
    }
}