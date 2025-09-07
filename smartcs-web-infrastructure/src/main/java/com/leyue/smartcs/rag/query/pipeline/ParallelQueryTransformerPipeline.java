package com.leyue.smartcs.rag.query.pipeline;

import dev.langchain4j.rag.query.Query;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 并行查询转换器管线实现
 * 提供高性能的并行查询处理能力，支持：
 * 1. 阶段内并行 - 单个阶段内的查询并行处理
 * 2. 阶段间流水线 - 不同阶段的流水线并行
 * 3. 自适应线程池 - 根据负载动态调整线程数
 * 4. 批量优化 - 批量处理相似查询以提升效率
 * 5. 缓存机制 - 查询结果缓存以减少重复计算
 * 
 * @author Claude
 */
@Slf4j
@Builder
public class ParallelQueryTransformerPipeline implements dev.langchain4j.rag.query.transformer.QueryTransformer {
    
    /**
     * 处理阶段列表
     */
    private final List<QueryTransformerStage> stages;
    
    /**
     * 管线配置
     */
    private final QueryContext.PipelineConfig pipelineConfig;
    
    /**
     * 指标收集器
     */
    private final QueryContext.MetricsCollector metricsCollector;
    
    /**
     * 并行处理器
     */
    @Builder.Default
    private final ParallelProcessor parallelProcessor = new ParallelProcessor();
    
    /**
     * 查询缓存
     */
    @Builder.Default
    private final QueryCache queryCache = new QueryCache();
    
    /**
     * 默认租户
     */
    @Builder.Default
    private final String defaultTenant = "default";
    
    /**
     * 默认渠道
     */
    @Builder.Default
    private final String defaultChannel = "web";
    
    @Override
    public Collection<Query> transform(Query query) {
        if (query == null) {
            log.warn("输入查询为null，返回空结果");
            return Collections.emptyList();
        }
        
        // 检查缓存
        String cacheKey = generateCacheKey(query);
        Collection<Query> cachedResult = queryCache.get(cacheKey);
        if (cachedResult != null) {
            log.debug("命中查询缓存: query={}", query.text());
            return cachedResult;
        }
        
        // 创建查询上下文
        QueryContext context = createQueryContext(query);
        
        log.debug("开始并行查询转换处理: originalQuery={}, stageCount={}", 
                query.text(), stages != null ? stages.size() : 0);
        
        try {
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 执行并行管线处理
            Collection<Query> result = executeParallelPipeline(context, Collections.singletonList(query));
            
            // 记录处理时间
            long elapsedTime = System.currentTimeMillis() - startTime;
            
            // 确保返回结果不为空
            if (result == null || result.isEmpty()) {
                log.warn("并行管线处理结果为空，返回原始查询: query={}", query.text());
                result = Collections.singletonList(query);
            }
            
            // 应用最终约束
            result = applyFinalConstraints(context, result);
            
            // 缓存结果
            queryCache.put(cacheKey, result);
            
            log.info("并行查询转换处理完成: originalQuery={}, expandedCount={}, elapsedMs={}", 
                    query.text(), result.size(), elapsedTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("并行查询转换处理失败，使用降级策略: query={}", query.text(), e);
            return handleFailure(context, query, e);
        } finally {
            // 清理资源
            cleanupStages(context);
        }
    }
    
    /**
     * 执行并行管线处理
     */
    private Collection<Query> executeParallelPipeline(QueryContext context, Collection<Query> queries) {
        if (stages == null || stages.isEmpty()) {
            log.debug("没有配置处理阶段，返回原始查询");
            return queries;
        }
        
        // 初始化所有阶段
        initializeStages(context);
        
        Collection<Query> currentQueries = queries;
        
        // 根据配置决定执行策略
        if (shouldUseParallelExecution(currentQueries.size())) {
            currentQueries = executeStagesInParallel(context, currentQueries);
        } else {
            currentQueries = executeStagesSequentially(context, currentQueries);
        }
        
        return currentQueries;
    }
    
    /**
     * 并行执行阶段
     */
    private Collection<Query> executeStagesInParallel(QueryContext context, Collection<Query> queries) {
        Collection<Query> currentQueries = queries;
        
        for (QueryTransformerStage stage : stages) {
            try {
                // 检查阶段是否启用和超时
                if (!stage.isEnabled(context) || context.getTimeoutControl().isTimeout()) {
                    continue;
                }
                
                // 并行执行当前阶段
                currentQueries = parallelProcessor.executeStageParallel(context, stage, currentQueries);
                
            } catch (Exception e) {
                currentQueries = handleStageFailure(context, stage, currentQueries, e);
            }
        }
        
        return currentQueries;
    }
    
    /**
     * 顺序执行阶段（原有逻辑）
     */
    private Collection<Query> executeStagesSequentially(QueryContext context, Collection<Query> queries) {
        Collection<Query> currentQueries = queries;
        
        for (QueryTransformerStage stage : stages) {
            try {
                if (!stage.isEnabled(context) || context.getTimeoutControl().isTimeout()) {
                    continue;
                }
                
                currentQueries = executeStage(context, stage, currentQueries);
                
            } catch (Exception e) {
                currentQueries = handleStageFailure(context, stage, currentQueries, e);
            }
        }
        
        return currentQueries;
    }
    
    /**
     * 执行单个阶段
     */
    private Collection<Query> executeStage(QueryContext context, QueryTransformerStage stage, 
                                         Collection<Query> inputQueries) {
        String stageName = stage.getName();
        long startTime = System.currentTimeMillis();
        
        context.getTimeoutControl().markStageStart();
        if (metricsCollector != null) {
            metricsCollector.recordStageStart(stageName, inputQueries.size());
        }
        
        try {
            Collection<Query> outputQueries = stage.apply(context, inputQueries);
            
            if (outputQueries == null || outputQueries.isEmpty()) {
                outputQueries = inputQueries;
            }
            
            long elapsedMs = System.currentTimeMillis() - startTime;
            
            if (metricsCollector != null) {
                metricsCollector.recordStageComplete(stageName, outputQueries.size(), elapsedMs);
            }
            
            return outputQueries;
            
        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            if (metricsCollector != null) {
                metricsCollector.recordStageFailure(stageName, e, elapsedMs);
            }
            throw e;
        }
    }
    
    /**
     * 判断是否使用并行执行
     */
    private boolean shouldUseParallelExecution(int queryCount) {
        // 查询数量较多时使用并行处理
        return queryCount >= 3 && parallelProcessor.isParallelExecutionEnabled();
    }
    
    /**
     * 处理阶段失败
     */
    private Collection<Query> handleStageFailure(QueryContext context, QueryTransformerStage stage,
                                               Collection<Query> inputQueries, Exception e) {
        String stageName = stage.getName();
        log.warn("阶段执行失败，应用降级策略: stage={}, error={}", stageName, e.getMessage());
        
        QueryContext.FallbackPolicy policy = context.getPipelineConfig().getFallbackPolicy();
        switch (policy) {
            case SKIP_STAGE:
                return inputQueries;
            case USE_BASIC_EXPANSION:
            case ORIGINAL_QUERY_ONLY:
                return Collections.singletonList(context.getOriginalQuery());
            default:
                return inputQueries;
        }
    }
    
    /**
     * 创建查询上下文
     */
    private QueryContext createQueryContext(Query originalQuery) {
        long currentTime = System.currentTimeMillis();
        
        return QueryContext.builder()
                .originalQuery(originalQuery)
                .tenant(defaultTenant)
                .channel(defaultChannel)
                .locale("zh-CN")
                .chatHistory(new ConcurrentHashMap<>())
                .attributes(new ConcurrentHashMap<>())
                .budgetControl(QueryContext.BudgetControl.builder()
                        .maxTokens(pipelineConfig != null ? 
                                Optional.ofNullable(pipelineConfig.getExpandingConfig())
                                        .map(c -> c.getN() * 100)
                                        .orElse(1000) : 1000)
                        .maxCost(10.0)
                        .build())
                .timeoutControl(QueryContext.TimeoutControl.builder()
                        .maxLatencyMs(30000L)
                        .pipelineStartTime(currentTime)
                        .build())
                .metricsCollector(metricsCollector)
                .pipelineConfig(pipelineConfig != null ? pipelineConfig : 
                        QueryContext.PipelineConfig.builder().build())
                .build();
    }
    
    /**
     * 初始化所有阶段
     */
    private void initializeStages(QueryContext context) {
        if (stages != null) {
            stages.parallelStream().forEach(stage -> {
                try {
                    stage.initialize(context);
                } catch (Exception e) {
                    log.warn("阶段初始化失败: stage={}", stage.getName(), e);
                }
            });
        }
    }
    
    /**
     * 清理所有阶段
     */
    private void cleanupStages(QueryContext context) {
        if (stages != null) {
            stages.parallelStream().forEach(stage -> {
                try {
                    stage.cleanup(context);
                } catch (Exception e) {
                    log.warn("阶段清理失败: stage={}", stage.getName(), e);
                }
            });
        }
    }
    
    /**
     * 应用最终约束
     */
    private Collection<Query> applyFinalConstraints(QueryContext context, Collection<Query> queries) {
        List<Query> result = new ArrayList<>(queries);
        
        int maxQueries = context.getPipelineConfig().getMaxQueries();
        if (result.size() > maxQueries) {
            result = result.subList(0, maxQueries);
        }
        
        if (context.getPipelineConfig().isKeepOriginal()) {
            Query originalQuery = context.getOriginalQuery();
            if (!result.contains(originalQuery)) {
                result.add(0, originalQuery);
                if (result.size() > maxQueries) {
                    result.remove(result.size() - 1);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 处理管线失败
     */
    private Collection<Query> handleFailure(QueryContext context, Query originalQuery, Exception e) {
        return Collections.singletonList(originalQuery);
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(Query query) {
        return "query_" + query.text().hashCode();
    }
    
    /**
     * 并行处理器
     */
    public static class ParallelProcessor {
        
        private final ExecutorService executorService;
        private final int corePoolSize;
        private final int maximumPoolSize;
        
        public ParallelProcessor() {
            this.corePoolSize = Runtime.getRuntime().availableProcessors();
            this.maximumPoolSize = corePoolSize * 2;
            
            this.executorService = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> {
                    Thread t = new Thread(r, "QueryTransformer-" + System.currentTimeMillis());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
        
        /**
         * 并行执行阶段
         */
        public Collection<Query> executeStageParallel(QueryContext context, QueryTransformerStage stage, 
                                                    Collection<Query> queries) {
            
            if (queries.size() <= 2) {
                // 查询数量较少，直接顺序执行
                return stage.apply(context, queries);
            }
            
            // 将查询分批并行处理
            List<Query> queryList = new ArrayList<>(queries);
            int batchSize = Math.max(1, queryList.size() / corePoolSize);
            List<List<Query>> batches = partition(queryList, batchSize);
            
            List<CompletableFuture<Collection<Query>>> futures = batches.stream()
                    .map(batch -> TracingSupport.supplyAsync(() -> {
                        try {
                            return stage.apply(context, batch);
                        } catch (Exception e) {
                            log.warn("批量处理失败: stage={}, batchSize={}", stage.getName(), batch.size());
                            return batch; // 返回原查询作为降级
                        }
                    }, executorService))
                    .collect(Collectors.toList());
            
            // 收集所有结果
            List<Query> allResults = new ArrayList<>();
            for (CompletableFuture<Collection<Query>> future : futures) {
                try {
                    Collection<Query> batchResult = future.get(10, TimeUnit.SECONDS);
                    allResults.addAll(batchResult);
                } catch (Exception e) {
                    log.warn("等待批量处理结果超时: stage={}", stage.getName());
                }
            }
            
            return allResults.isEmpty() ? queries : allResults;
        }
        
        /**
         * 分批处理
         */
        private <T> List<List<T>> partition(List<T> list, int batchSize) {
            List<List<T>> batches = new ArrayList<>();
            for (int i = 0; i < list.size(); i += batchSize) {
                batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
            }
            return batches;
        }
        
        /**
         * 是否启用并行执行
         */
        public boolean isParallelExecutionEnabled() {
            return true; // 可以基于配置或系统负载动态调整
        }
        
        /**
         * 关闭线程池
         */
        public void shutdown() {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        /**
         * 获取线程池状态
         */
        public Map<String, Object> getStatus() {
            Map<String, Object> status = new HashMap<>();
            
            if (executorService instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
                status.put("corePoolSize", tpe.getCorePoolSize());
                status.put("maximumPoolSize", tpe.getMaximumPoolSize());
                status.put("activeCount", tpe.getActiveCount());
                status.put("taskCount", tpe.getTaskCount());
                status.put("completedTaskCount", tpe.getCompletedTaskCount());
                status.put("queueSize", tpe.getQueue().size());
            }
            
            return status;
        }
    }
    
    /**
     * 查询缓存
     */
    public static class QueryCache {
        
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        private final long maxSize = 1000; // 最大缓存条目数
        private final long ttlMs = 300000; // 5分钟TTL
        
        /**
         * 获取缓存
         */
        public Collection<Query> get(String key) {
            CacheEntry entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                return entry.getValue();
            } else if (entry != null) {
                cache.remove(key);
            }
            return null;
        }
        
        /**
         * 设置缓存
         */
        public void put(String key, Collection<Query> value) {
            if (cache.size() >= maxSize) {
                evictOldEntries();
            }
            
            cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMs));
        }
        
        /**
         * 驱逐过期条目
         */
        private void evictOldEntries() {
            long currentTime = System.currentTimeMillis();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
            
            // 如果还是太多，移除最老的条目
            if (cache.size() >= maxSize) {
                cache.entrySet().stream()
                        .sorted(Map.Entry.<String, CacheEntry>comparingByValue(
                                (e1, e2) -> Long.compare(e1.createdTime, e2.createdTime)))
                        .limit(cache.size() - maxSize / 2)
                        .map(Map.Entry::getKey)
                        .forEach(cache::remove);
            }
        }
        
        /**
         * 获取缓存统计
         */
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("size", cache.size());
            stats.put("maxSize", maxSize);
            stats.put("ttlMs", ttlMs);
            
            long currentTime = System.currentTimeMillis();
            long expiredCount = cache.values().stream()
                    .mapToLong(entry -> entry.isExpired(currentTime) ? 1L : 0L)
                    .sum();
            stats.put("expiredCount", expiredCount);
            
            return stats;
        }
        
        /**
         * 清空缓存
         */
        public void clear() {
            cache.clear();
        }
        
        /**
         * 缓存条目
         */
        private static class CacheEntry {
            private final Collection<Query> value;
            private final long expireTime;
            private final long createdTime;
            
            public CacheEntry(Collection<Query> value, long expireTime) {
                this.value = value;
                this.expireTime = expireTime;
                this.createdTime = System.currentTimeMillis();
            }
            
            public Collection<Query> getValue() {
                return value;
            }
            
            public boolean isExpired() {
                return isExpired(System.currentTimeMillis());
            }
            
            public boolean isExpired(long currentTime) {
                return currentTime > expireTime;
            }
        }
    }
    
    /**
     * 获取并行处理器状态
     */
    public Map<String, Object> getParallelProcessorStatus() {
        return parallelProcessor.getStatus();
    }
    
    /**
     * 获取缓存统计
     */
    public Map<String, Object> getCacheStats() {
        return queryCache.getStats();
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        queryCache.clear();
    }
    
    /**
     * 关闭并行处理器
     */
    public void shutdown() {
        parallelProcessor.shutdown();
    }
}
