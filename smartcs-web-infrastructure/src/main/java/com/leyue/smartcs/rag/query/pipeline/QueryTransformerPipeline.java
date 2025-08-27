package com.leyue.smartcs.rag.query.pipeline;

import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 查询转换器管线实现
 * 基于阶段化处理的查询转换管线，支持可配置、可观测、可降级的查询优化
 * 
 * @author Claude
 */
@Slf4j
@Builder
public class QueryTransformerPipeline implements QueryTransformer {
    
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
        // 处理null输入
        if (query == null) {
            log.warn("输入查询为null，返回空结果");
            return Collections.emptyList();
        }
        
        // 创建查询上下文
        QueryContext context = createQueryContext(query);
        
        log.debug("开始查询转换管线处理: originalQuery={}, stageCount={}", 
                query.text(), stages != null ? stages.size() : 0);
        
        try {
            // 初始化指标收集
            if (metricsCollector != null) {
                metricsCollector.recordStageStart("PIPELINE_START", 1);
            }
            
            // 执行管线处理
            Collection<Query> result = executeStages(context, Collections.singletonList(query));
            
            // 确保返回结果不为空
            if (result == null || result.isEmpty()) {
                log.warn("管线处理结果为空，返回原始查询: query={}", query.text());
                result = Collections.singletonList(query);
            }
            
            // 应用最终约束
            result = applyFinalConstraints(context, result);
            
            log.info("查询转换管线处理完成: originalQuery={}, expandedCount={}", 
                    query.text(), result.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("查询转换管线处理失败，使用降级策略: query={}", query.text(), e);
            return handleFailure(context, query, e);
        } finally {
            // 清理资源
            cleanupStages(context);
        }
    }

    /**
     * 调试/测试入口：执行转换并记录各阶段前后变化
     */
    public QueryTransformationTrace transformWithTrace(Query query) {
        if (query == null) {
            return QueryTransformationTrace.builder()
                    .originalQuery(null)
                    .finalQueries(Collections.emptyList())
                    .build();
        }

        QueryContext context = createQueryContext(query);
        // 在 attributes 中挂载 trace 容器
        QueryTransformationTrace trace = QueryTransformationTrace.builder()
                .originalQuery(query.text())
                .build();
        context.setAttribute(TRACE_KEY, trace);

        try {
            if (metricsCollector != null) {
                metricsCollector.recordStageStart("PIPELINE_START", 1);
            }

            Collection<Query> result = executeStages(context, Collections.singletonList(query));
            if (result == null || result.isEmpty()) {
                result = Collections.singletonList(query);
            }
            result = applyFinalConstraints(context, result);

            // 记录最终输出
            trace.setFinalQueries(toTextList(result));
            return trace;
        } catch (Exception e) {
            // 失败降级时也记录最终输出为原始
            trace.setFinalQueries(Collections.singletonList(query.text()));
            return trace;
        } finally {
            cleanupStages(context);
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
                                        .map(c -> c.getN() * 100)  // 估算token预算
                                        .orElse(1000) : 1000)
                        .maxCost(10.0) // 默认最大成本10美元
                        .build())
                .timeoutControl(QueryContext.TimeoutControl.builder()
                        .maxLatencyMs(30000L) // 默认30秒超时
                        .pipelineStartTime(currentTime)
                        .build())
                .metricsCollector(metricsCollector)
                .pipelineConfig(pipelineConfig != null ? pipelineConfig : 
                        QueryContext.PipelineConfig.builder().build())
                .build();
    }
    
    /**
     * 执行管线阶段
     */
    private Collection<Query> executeStages(QueryContext context, Collection<Query> queries) {
        if (stages == null || stages.isEmpty()) {
            log.debug("没有配置处理阶段，返回原始查询");
            return queries;
        }
        
        Collection<Query> currentQueries = queries;
        
        // 初始化所有阶段
        initializeStages(context);
        
        for (QueryTransformerStage stage : stages) {
            try {
                // 检查是否启用当前阶段
                if (!stage.isEnabled(context)) {
                    log.debug("阶段已禁用，跳过: stage={}", stage.getName());
                    if (metricsCollector != null) {
                        metricsCollector.recordStageSkipped(stage.getName(), "阶段已禁用");
                    }
                    // 记录trace：禁用跳过
                    recordStageTrace(context, stage.getName(), currentQueries, currentQueries, 0L,
                            "skipped: disabled");
                    continue;
                }
                
                // 检查超时
                if (context.getTimeoutControl().isTimeout()) {
                    log.warn("管线执行超时，中断处理: stage={}, remainingMs={}", 
                            stage.getName(), context.getTimeoutControl().getRemainingTimeMs());
                    if (metricsCollector != null) {
                        metricsCollector.recordStageSkipped(stage.getName(), "超时中断");
                    }
                    // 记录trace：超时跳过
                    recordStageTrace(context, stage.getName(), currentQueries, currentQueries, 0L,
                            "skipped: timeout");
                    break;
                }
                
                // 执行阶段处理
                currentQueries = executeStage(context, stage, currentQueries);
                
            } catch (QueryTransformationException e) {
                // 处理可恢复的转换错误
                currentQueries = handleStageFailure(context, stage, currentQueries, e);
            } catch (Exception e) {
                // 处理不可预期的错误
                log.error("阶段执行发生未预期错误: stage={}", stage.getName(), e);
                currentQueries = handleStageFailure(context, stage, currentQueries, 
                        new QueryTransformationException(stage.getName(), "阶段执行失败", e));
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
        
        // 记录阶段开始
        context.getTimeoutControl().markStageStart();
        if (metricsCollector != null) {
            metricsCollector.recordStageStart(stageName, inputQueries.size());
        }
        
        log.debug("执行转换阶段: stage={}, inputCount={}", stageName, inputQueries.size());
        
        try {
            // 执行阶段转换
            Collection<Query> outputQueries = stage.apply(context, inputQueries);
            
            // 检查输出结果
            if (outputQueries == null || outputQueries.isEmpty()) {
                log.warn("阶段输出为空，使用输入查询: stage={}", stageName);
                outputQueries = inputQueries;
            }
            
            long elapsedMs = System.currentTimeMillis() - startTime;
            
            // 记录阶段完成
            if (metricsCollector != null) {
                metricsCollector.recordStageComplete(stageName, outputQueries.size(), elapsedMs);
            }
            
            log.debug("转换阶段执行完成: stage={}, inputCount={}, outputCount={}, elapsedMs={}", 
                    stageName, inputQueries.size(), outputQueries.size(), elapsedMs);

            // 记录 trace
            recordStageTrace(context, stageName, inputQueries, outputQueries, elapsedMs, null);
            
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
     * 处理阶段失败
     */
    private Collection<Query> handleStageFailure(QueryContext context, QueryTransformerStage stage,
                                               Collection<Query> inputQueries, QueryTransformationException e) {
        String stageName = stage.getName();
        
        log.warn("阶段执行失败，应用降级策略: stage={}, error={}, recoverable={}", 
                stageName, e.getMessage(), e.isRecoverable());
        
        // 如果错误不可恢复，直接返回输入查询
        if (!e.isRecoverable()) {
            log.error("阶段错误不可恢复，跳过处理: stage={}", stageName);
            // 记录 trace：失败（不可恢复）
            recordStageTrace(context, stageName, inputQueries, inputQueries, 0L,
                    "failure: non-recoverable - " + safeMsg(e));
            return inputQueries;
        }
        
        // 根据降级策略处理
        QueryContext.FallbackPolicy policy = context.getPipelineConfig().getFallbackPolicy();
        switch (policy) {
            case SKIP_STAGE:
                log.info("跳过失败的阶段: stage={}", stageName);
                recordStageTrace(context, stageName, inputQueries, inputQueries, 0L,
                        "failure: skipped - " + safeMsg(e));
                return inputQueries;
            
            case USE_BASIC_EXPANSION:
                log.info("使用基础扩展降级: stage={}", stageName);
                // 使用基础扩展降级策略 - 返回原始查询
                Collection<Query> basic = Collections.singletonList(context.getOriginalQuery());
                recordStageTrace(context, stageName, inputQueries, basic, 0L,
                        "failure: basic expansion - " + safeMsg(e));
                return basic;
            
            case ORIGINAL_QUERY_ONLY:
                log.info("回退到原始查询: stage={}", stageName);
                Collection<Query> orig = Collections.singletonList(context.getOriginalQuery());
                recordStageTrace(context, stageName, inputQueries, orig, 0L,
                        "failure: original only - " + safeMsg(e));
                return orig;
            
            default:
                return inputQueries;
        }
    }

    private String safeMsg(Throwable e) {
        try { return e.getMessage(); } catch (Exception ex) { return ""; }
    }

    private static final String TRACE_KEY = "query-transformer-trace";

    /**
     * 若存在 trace 上下文，记录单阶段 before/after 对比
     */
    @SuppressWarnings("unchecked")
    private void recordStageTrace(QueryContext context, String stageName,
                                  Collection<Query> before,
                                  Collection<Query> after,
                                  long elapsedMs,
                                  String note) {
        if (context == null) return;
        Object obj = context.getAttribute(TRACE_KEY);
        if (!(obj instanceof QueryTransformationTrace)) return;
        QueryTransformationTrace trace = (QueryTransformationTrace) obj;

        List<String> beforeTexts = toTextList(before);
        List<String> afterTexts = toTextList(after);

        // 计算 added/removed/unchanged（按小写去重比对）
        java.util.Set<String> beforeSet = new java.util.LinkedHashSet<>();
        for (String s : beforeTexts) beforeSet.add(norm(s));
        java.util.Set<String> afterSet = new java.util.LinkedHashSet<>();
        for (String s : afterTexts) afterSet.add(norm(s));

        List<String> added = new java.util.ArrayList<>();
        for (String s : afterTexts) if (!beforeSet.contains(norm(s))) added.add(s);
        List<String> removed = new java.util.ArrayList<>();
        for (String s : beforeTexts) if (!afterSet.contains(norm(s))) removed.add(s);
        List<String> unchanged = new java.util.ArrayList<>();
        for (String s : beforeTexts) if (afterSet.contains(norm(s))) unchanged.add(s);

        QueryTransformationTrace.StageTrace stageTrace = QueryTransformationTrace.StageTrace.builder()
                .stage(stageName)
                .before(beforeTexts)
                .after(afterTexts)
                .added(added)
                .removed(removed)
                .unchanged(unchanged)
                .elapsedMs(elapsedMs)
                .note(note)
                .build();

        trace.getStages().add(stageTrace);
    }

    private String norm(String s) { return s == null ? "" : s.trim().toLowerCase(); }

    private List<String> toTextList(Collection<Query> queries) {
        List<String> list = new ArrayList<>();
        if (queries == null) return list;
        for (Query q : queries) {
            if (q != null && q.text() != null) list.add(q.text());
        }
        return list;
    }
    
    /**
     * 初始化所有阶段
     */
    private void initializeStages(QueryContext context) {
        if (stages == null) {
            return;
        }
        
        for (QueryTransformerStage stage : stages) {
            try {
                stage.initialize(context);
            } catch (Exception e) {
                log.warn("阶段初始化失败: stage={}", stage.getName(), e);
            }
        }
    }
    
    /**
     * 清理所有阶段
     */
    private void cleanupStages(QueryContext context) {
        if (stages == null) {
            return;
        }
        
        for (QueryTransformerStage stage : stages) {
            try {
                stage.cleanup(context);
            } catch (Exception e) {
                log.warn("阶段清理失败: stage={}", stage.getName(), e);
            }
        }
    }
    
    /**
     * 应用最终约束
     */
    private Collection<Query> applyFinalConstraints(QueryContext context, Collection<Query> queries) {
        List<Query> result = new ArrayList<>(queries);
        
        // 应用最大查询数量限制
        int maxQueries = context.getPipelineConfig().getMaxQueries();
        if (result.size() > maxQueries) {
            log.debug("应用最大查询数量限制: current={}, max={}", result.size(), maxQueries);
            result = result.subList(0, maxQueries);
        }
        
        // 确保包含原始查询（如果配置要求）
        if (context.getPipelineConfig().isKeepOriginal()) {
            Query originalQuery = context.getOriginalQuery();
            if (!result.contains(originalQuery)) {
                // 将原始查询插入到第一位
                result.add(0, originalQuery);
                // 如果超出最大数量限制，移除最后一个
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
        QueryContext.FallbackPolicy policy = context.getPipelineConfig().getFallbackPolicy();
        
        switch (policy) {
            case ORIGINAL_QUERY_ONLY:
                log.info("管线失败，返回原始查询: query={}", originalQuery.text());
                return Collections.singletonList(originalQuery);
            
            case USE_BASIC_EXPANSION:
            case SKIP_STAGE:
            default:
                log.info("管线失败，返回原始查询作为降级: query={}", originalQuery.text());
                return Collections.singletonList(originalQuery);
        }
    }
}
