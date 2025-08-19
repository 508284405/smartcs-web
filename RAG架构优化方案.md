# RAG架构优化方案：简化动态工厂 + 缓存优化

## 背景分析

### 当前问题
1. **静态ChatModel依赖**：`RagChatServiceConfig` 中直接注入了 `ChatModel` 和 `StreamingChatModel`，这些是静态的单例Bean，无法根据用户参数动态切换模型。

2. **Spring单例Bean限制**：所有RAG组件（`RetrievalAugmentor`、`ContentInjector`、`QueryTransformer`、`QueryRouter`、`ReRankingContentAggregator`）都被配置为Spring单例Bean，无法根据不同的ChatModel动态构建。

3. **服务实例静态化**：`SmartChatService`、`ModelInferenceService` 等AI服务也被配置为Spring单例Bean，无法根据用户参数动态创建。

### 现有优势
1. **DynamicModelManager**：已经实现了根据modelId动态获取ChatModel和StreamingChatModel的能力，并支持缓存机制。

2. **AiServices框架**：已经使用LangChain4j的AiServices框架进行声明式AI服务创建。

3. **RAG组件基础**：已经定义了各种RAG组件的基础结构。

## 优化方案：简化动态工厂 + 缓存优化

### 核心思路
- 在 `DynamicModelManager` 中扩展RAG组件创建能力
- 为每个modelId创建独立的RAG组件实例
- 使用多层缓存优化性能
- 移除所有Spring单例Bean配置，改为按需创建

### 优势
1. **最小化变更**：在现有架构基础上扩展，风险较低
2. **性能优化**：通过缓存机制保证性能
3. **职责清晰**：DynamicModelManager已经负责模型管理，扩展RAG能力符合职责
4. **易于实现**：实现相对简单，容易理解和维护

## 技术实现

### 1. 扩展DynamicModelManager

#### 1.1 添加RAG组件缓存字段
```java
// 在DynamicModelManager中添加RAG组件缓存
private final Map<Long, RetrievalAugmentor> retrievalAugmentorCache = new ConcurrentHashMap<>();
private final Map<Long, ContentInjector> contentInjectorCache = new ConcurrentHashMap<>();
private final Map<Long, QueryTransformer> queryTransformerCache = new ConcurrentHashMap<>();
private final Map<Long, QueryRouter> queryRouterCache = new ConcurrentHashMap<>();
private final Map<Long, ReRankingContentAggregator> contentAggregatorCache = new ConcurrentHashMap<>();
```

#### 1.2 添加RAG组件创建方法
```java
/**
 * 根据模型ID创建RetrievalAugmentor
 */
public RetrievalAugmentor createRetrievalAugmentor(Long modelId) {
    return retrievalAugmentorCache.computeIfAbsent(modelId, id -> {
        log.debug("创建RetrievalAugmentor实例: modelId={}", id);
        ChatModel chatModel = getChatModel(id);
        
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
```

### 2. 优化AI服务创建

#### 2.1 修改createModelInferenceService方法
```java
/**
 * 创建模型推理服务 - 集成完整RAG能力
 */
public ModelInferenceService createModelInferenceService(Long modelId, List<Long> knowledgeIds) {
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
```

#### 2.2 添加SmartChatService创建方法
```java
/**
 * 创建智能聊天服务
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
```

#### 2.3 添加StructuredChatService创建方法
```java
/**
 * 创建结构化聊天服务
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
```

### 3. 缓存管理优化

#### 3.1 添加缓存清理方法
```java
/**
 * 清除指定模型的RAG组件缓存
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
```

#### 3.2 添加缓存统计方法
```java
/**
 * 获取RAG组件缓存统计信息
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
```

### 4. 移除Spring单例Bean配置

#### 4.1 修改RagChatServiceConfig
```java
/**
 * 智能聊天服务配置 - 动态创建模式
 * 移除所有Spring单例Bean配置，改为动态创建
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RagChatServiceConfig {

    private final ChatMemoryStore chatMemoryStore;
    private final DynamicModelManager dynamicModelManager;
    private final EmbeddingStore<TextSegment> embeddingStore;

    // 移除所有@Bean方法，改为通过DynamicModelManager动态创建
    
    /**
     * 获取DynamicModelManager实例
     * 用于动态创建RAG组件和AI服务
     */
    @Bean
    public DynamicModelManager dynamicModelManager() {
        return dynamicModelManager;
    }
}
```

### 5. 更新服务调用层

#### 5.1 修改ModelInferenceGatewayImpl
```java
/**
 * 创建推理服务 - 使用动态创建模式
 */
private ModelInferenceService createInferenceService(Long modelId, List<Long> knowledgeIds) {
    try {
        // 使用DynamicModelManager动态创建推理服务
        return dynamicModelManager.createModelInferenceService(modelId, knowledgeIds);
    } catch (Exception e) {
        log.error("创建推理服务失败: modelId={}, knowledgeIds={}", modelId, knowledgeIds, e);
        throw new BizException("无法创建推理服务: " + e.getMessage());
    }
}
```

## 实施步骤

### 第一阶段：扩展DynamicModelManager
1. 添加RAG组件缓存字段
2. 实现RAG组件创建方法
3. 优化AI服务创建方法
4. 添加缓存管理方法

### 第二阶段：移除Spring配置
1. 修改RagChatServiceConfig，移除所有@Bean方法
2. 移除对静态ChatModel的依赖注入
3. 更新相关服务调用

### 第三阶段：测试验证
1. 测试动态模型切换功能
2. 验证RAG组件缓存机制
3. 性能测试和优化

## 预期效果

### 功能改进
1. **完全支持动态模型切换**：用户可以根据需要选择不同的模型进行推理
2. **RAG组件隔离**：每个模型都有独立的RAG组件实例，避免冲突
3. **配置灵活性**：支持为不同模型配置不同的RAG参数

### 性能优化
1. **多层缓存**：模型实例和RAG组件都有独立的缓存机制
2. **按需创建**：只有在需要时才创建组件实例，节省内存
3. **缓存管理**：支持缓存清理和统计，便于监控和优化

### 架构优势
1. **职责清晰**：DynamicModelManager统一管理所有模型相关组件
2. **易于扩展**：新增模型或RAG组件都很容易
3. **维护简单**：集中管理，减少配置复杂度

## 风险评估

### 低风险
- 在现有架构基础上扩展，不会破坏现有功能
- 使用成熟的缓存机制，性能有保障
- 分阶段实施，可以及时发现问题

### 需要关注
- 内存使用：多个模型实例可能增加内存占用
- 缓存策略：需要根据实际使用情况调整缓存策略
- 错误处理：需要完善异常处理和降级机制

## 总结

本方案通过扩展DynamicModelManager的方式，实现了RAG架构的动态化，既保持了现有架构的优势，又解决了静态配置的限制。通过多层缓存机制，既保证了性能，又支持了灵活的模型切换。这是一个风险较低、收益较高的优化方案。 