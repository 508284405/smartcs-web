package com.leyue.smartcs.rag.query.pipeline;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.Map;

/**
 * 默认指标收集器实现
 * 提供查询转换管线的全面性能监控和指标收集功能
 * 
 * @author Claude
 */
@Slf4j
public class DefaultMetricsCollector implements QueryContext.MetricsCollector {
    
    /**
     * 阶段执行统计
     */
    private final Map<String, StageMetrics> stageMetrics = new ConcurrentHashMap<>();
    
    /**
     * 全局统计
     */
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);
    private final DoubleAdder totalElapsedMs = new DoubleAdder();
    private final AtomicInteger totalInputQueries = new AtomicInteger(0);
    private final AtomicInteger totalOutputQueries = new AtomicInteger(0);
    private final DoubleAdder totalTokensConsumed = new DoubleAdder();
    private final DoubleAdder totalCostConsumed = new DoubleAdder();
    
    @Override
    public void recordStageStart(String stageName, int inputQueryCount) {
        StageMetrics metrics = getOrCreateStageMetrics(stageName);
        metrics.executions.incrementAndGet();
        totalExecutions.incrementAndGet();
        totalInputQueries.addAndGet(inputQueryCount);
        
        log.debug("阶段开始执行: stage={}, inputQueries={}", stageName, inputQueryCount);
    }
    
    @Override
    public void recordStageComplete(String stageName, int outputQueryCount, long elapsedMs) {
        StageMetrics metrics = getOrCreateStageMetrics(stageName);
        metrics.successes.incrementAndGet();
        metrics.totalElapsedMs.add(elapsedMs);
        metrics.totalOutputQueries.addAndGet(outputQueryCount);
        
        // 更新最小/最大执行时间
        updateMinMax(metrics, elapsedMs);
        
        totalElapsedMs.add(elapsedMs);
        totalOutputQueries.addAndGet(outputQueryCount);
        
        log.debug("阶段执行完成: stage={}, outputQueries={}, elapsedMs={}", 
                stageName, outputQueryCount, elapsedMs);
    }
    
    @Override
    public void recordStageFailure(String stageName, Throwable error, long elapsedMs) {
        StageMetrics metrics = getOrCreateStageMetrics(stageName);
        metrics.failures.incrementAndGet();
        metrics.totalElapsedMs.add(elapsedMs);
        
        // 记录错误类型
        String errorType = error.getClass().getSimpleName();
        metrics.errorCounts.merge(errorType, 1, Integer::sum);
        
        totalFailures.incrementAndGet();
        totalElapsedMs.add(elapsedMs);
        
        log.warn("阶段执行失败: stage={}, error={}, elapsedMs={}", 
                stageName, error.getMessage(), elapsedMs);
    }
    
    @Override
    public void recordStageSkipped(String stageName, String reason) {
        StageMetrics metrics = getOrCreateStageMetrics(stageName);
        metrics.skipped.incrementAndGet();
        
        // 记录跳过原因
        metrics.skipReasons.merge(reason, 1, Integer::sum);
        
        log.debug("阶段被跳过: stage={}, reason={}", stageName, reason);
    }
    
    @Override
    public void recordTokensConsumption(String stageName, int inputTokens, int outputTokens) {
        StageMetrics metrics = getOrCreateStageMetrics(stageName);
        metrics.inputTokens.add(inputTokens);
        metrics.outputTokens.add(outputTokens);
        
        totalTokensConsumed.add(inputTokens + outputTokens);
        
        log.debug("阶段Token消耗: stage={}, inputTokens={}, outputTokens={}", 
                stageName, inputTokens, outputTokens);
    }
    
    @Override
    public void recordCostConsumption(String stageName, double cost) {
        StageMetrics metrics = getOrCreateStageMetrics(stageName);
        metrics.totalCost.add(cost);
        
        totalCostConsumed.add(cost);
        
        log.debug("阶段成本消耗: stage={}, cost=${:.4f}", stageName, cost);
    }
    
    /**
     * 获取或创建阶段指标
     */
    private StageMetrics getOrCreateStageMetrics(String stageName) {
        return stageMetrics.computeIfAbsent(stageName, k -> new StageMetrics());
    }
    
    /**
     * 更新最小/最大执行时间
     */
    private void updateMinMax(StageMetrics metrics, long elapsedMs) {
        metrics.minElapsedMs.updateAndGet(current -> current == 0 ? elapsedMs : Math.min(current, elapsedMs));
        metrics.maxElapsedMs.updateAndGet(current -> Math.max(current, elapsedMs));
    }
    
    /**
     * 获取全局统计信息
     */
    public GlobalMetrics getGlobalMetrics() {
        return GlobalMetrics.builder()
                .totalExecutions(totalExecutions.get())
                .totalFailures(totalFailures.get())
                .totalElapsedMs(totalElapsedMs.sum())
                .averageElapsedMs(totalExecutions.get() > 0 ? totalElapsedMs.sum() / totalExecutions.get() : 0)
                .successRate(calculateSuccessRate())
                .totalInputQueries(totalInputQueries.get())
                .totalOutputQueries(totalOutputQueries.get())
                .averageExpansionRatio(calculateAverageExpansionRatio())
                .totalTokensConsumed(totalTokensConsumed.sum())
                .totalCostConsumed(totalCostConsumed.sum())
                .build();
    }
    
    /**
     * 获取指定阶段的统计信息
     */
    public StageMetricsSummary getStageMetrics(String stageName) {
        StageMetrics metrics = stageMetrics.get(stageName);
        if (metrics == null) {
            return null;
        }
        
        long executions = metrics.executions.get();
        double avgElapsedMs = executions > 0 ? metrics.totalElapsedMs.sum() / executions : 0;
        double stageSuccessRate = executions > 0 ? (double) metrics.successes.get() / executions : 0;
        
        return StageMetricsSummary.builder()
                .stageName(stageName)
                .executions(executions)
                .successes(metrics.successes.get())
                .failures(metrics.failures.get())
                .skipped(metrics.skipped.get())
                .successRate(stageSuccessRate)
                .totalElapsedMs(metrics.totalElapsedMs.sum())
                .averageElapsedMs(avgElapsedMs)
                .minElapsedMs(metrics.minElapsedMs.get())
                .maxElapsedMs(metrics.maxElapsedMs.get())
                .totalOutputQueries(metrics.totalOutputQueries.get())
                .inputTokens(metrics.inputTokens.sum())
                .outputTokens(metrics.outputTokens.sum())
                .totalCost(metrics.totalCost.sum())
                .errorCounts(Map.copyOf(metrics.errorCounts))
                .skipReasons(Map.copyOf(metrics.skipReasons))
                .build();
    }
    
    /**
     * 获取所有阶段的统计信息
     */
    public Map<String, StageMetricsSummary> getAllStageMetrics() {
        Map<String, StageMetricsSummary> result = new ConcurrentHashMap<>();
        stageMetrics.forEach((stageName, metrics) -> {
            result.put(stageName, getStageMetrics(stageName));
        });
        return result;
    }
    
    /**
     * 重置所有统计信息
     */
    public void reset() {
        stageMetrics.clear();
        totalExecutions.set(0);
        totalFailures.set(0);
        totalElapsedMs.reset();
        totalInputQueries.set(0);
        totalOutputQueries.set(0);
        totalTokensConsumed.reset();
        totalCostConsumed.reset();
        
        log.info("指标收集器已重置");
    }
    
    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = totalExecutions.get();
        if (total == 0) return 0.0;
        return (double) (total - totalFailures.get()) / total;
    }
    
    /**
     * 计算平均扩展比率
     */
    private double calculateAverageExpansionRatio() {
        int inputQueries = totalInputQueries.get();
        if (inputQueries == 0) return 1.0;
        return (double) totalOutputQueries.get() / inputQueries;
    }
    
    /**
     * 打印统计报告
     */
    public void printMetricsReport() {
        GlobalMetrics global = getGlobalMetrics();
        
        log.info("=== 查询转换管线指标报告 ===");
        log.info("全局统计:");
        log.info("  总执行次数: {}", global.totalExecutions);
        log.info("  成功率: {:.2f}%", global.successRate * 100);
        log.info("  平均执行时间: {:.2f}ms", global.averageElapsedMs);
        log.info("  查询扩展比率: {:.2f}", global.averageExpansionRatio);
        log.info("  总Token消耗: {:.0f}", global.totalTokensConsumed);
        log.info("  总成本: ${:.4f}", global.totalCostConsumed);
        
        log.info("\n各阶段统计:");
        getAllStageMetrics().forEach((stageName, metrics) -> {
            log.info("  阶段 [{}]:", stageName);
            log.info("    执行次数: {} (成功: {}, 失败: {}, 跳过: {})", 
                    metrics.executions, metrics.successes, metrics.failures, metrics.skipped);
            log.info("    成功率: {:.2f}%", metrics.successRate * 100);
            log.info("    执行时间: 平均={:.2f}ms, 最小={:.2f}ms, 最大={:.2f}ms", 
                    metrics.averageElapsedMs, (double)metrics.minElapsedMs, (double)metrics.maxElapsedMs);
            if (metrics.totalCost > 0) {
                log.info("    成本: ${:.4f}", metrics.totalCost);
            }
            if (!metrics.errorCounts.isEmpty()) {
                log.info("    错误统计: {}", metrics.errorCounts);
            }
        });
    }
    
    /**
     * 阶段指标存储
     */
    private static class StageMetrics {
        final AtomicLong executions = new AtomicLong(0);
        final AtomicLong successes = new AtomicLong(0);
        final AtomicLong failures = new AtomicLong(0);
        final AtomicLong skipped = new AtomicLong(0);
        final DoubleAdder totalElapsedMs = new DoubleAdder();
        final AtomicLong minElapsedMs = new AtomicLong(0);
        final AtomicLong maxElapsedMs = new AtomicLong(0);
        final AtomicInteger totalOutputQueries = new AtomicInteger(0);
        final DoubleAdder inputTokens = new DoubleAdder();
        final DoubleAdder outputTokens = new DoubleAdder();
        final DoubleAdder totalCost = new DoubleAdder();
        final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();
        final Map<String, Integer> skipReasons = new ConcurrentHashMap<>();
    }
    
    /**
     * 全局指标摘要
     */
    public static class GlobalMetrics {
        public final long totalExecutions;
        public final long totalFailures;
        public final double totalElapsedMs;
        public final double averageElapsedMs;
        public final double successRate;
        public final int totalInputQueries;
        public final int totalOutputQueries;
        public final double averageExpansionRatio;
        public final double totalTokensConsumed;
        public final double totalCostConsumed;
        
        private GlobalMetrics(Builder builder) {
            this.totalExecutions = builder.totalExecutions;
            this.totalFailures = builder.totalFailures;
            this.totalElapsedMs = builder.totalElapsedMs;
            this.averageElapsedMs = builder.averageElapsedMs;
            this.successRate = builder.successRate;
            this.totalInputQueries = builder.totalInputQueries;
            this.totalOutputQueries = builder.totalOutputQueries;
            this.averageExpansionRatio = builder.averageExpansionRatio;
            this.totalTokensConsumed = builder.totalTokensConsumed;
            this.totalCostConsumed = builder.totalCostConsumed;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private long totalExecutions;
            private long totalFailures;
            private double totalElapsedMs;
            private double averageElapsedMs;
            private double successRate;
            private int totalInputQueries;
            private int totalOutputQueries;
            private double averageExpansionRatio;
            private double totalTokensConsumed;
            private double totalCostConsumed;
            
            public Builder totalExecutions(long totalExecutions) {
                this.totalExecutions = totalExecutions;
                return this;
            }
            
            public Builder totalFailures(long totalFailures) {
                this.totalFailures = totalFailures;
                return this;
            }
            
            public Builder totalElapsedMs(double totalElapsedMs) {
                this.totalElapsedMs = totalElapsedMs;
                return this;
            }
            
            public Builder averageElapsedMs(double averageElapsedMs) {
                this.averageElapsedMs = averageElapsedMs;
                return this;
            }
            
            public Builder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }
            
            public Builder totalInputQueries(int totalInputQueries) {
                this.totalInputQueries = totalInputQueries;
                return this;
            }
            
            public Builder totalOutputQueries(int totalOutputQueries) {
                this.totalOutputQueries = totalOutputQueries;
                return this;
            }
            
            public Builder averageExpansionRatio(double averageExpansionRatio) {
                this.averageExpansionRatio = averageExpansionRatio;
                return this;
            }
            
            public Builder totalTokensConsumed(double totalTokensConsumed) {
                this.totalTokensConsumed = totalTokensConsumed;
                return this;
            }
            
            public Builder totalCostConsumed(double totalCostConsumed) {
                this.totalCostConsumed = totalCostConsumed;
                return this;
            }
            
            public GlobalMetrics build() {
                return new GlobalMetrics(this);
            }
        }
    }
    
    /**
     * 阶段指标摘要
     */
    public static class StageMetricsSummary {
        public final String stageName;
        public final long executions;
        public final long successes;
        public final long failures;
        public final long skipped;
        public final double successRate;
        public final double totalElapsedMs;
        public final double averageElapsedMs;
        public final long minElapsedMs;
        public final long maxElapsedMs;
        public final int totalOutputQueries;
        public final double inputTokens;
        public final double outputTokens;
        public final double totalCost;
        public final Map<String, Integer> errorCounts;
        public final Map<String, Integer> skipReasons;
        
        private StageMetricsSummary(Builder builder) {
            this.stageName = builder.stageName;
            this.executions = builder.executions;
            this.successes = builder.successes;
            this.failures = builder.failures;
            this.skipped = builder.skipped;
            this.successRate = builder.successRate;
            this.totalElapsedMs = builder.totalElapsedMs;
            this.averageElapsedMs = builder.averageElapsedMs;
            this.minElapsedMs = builder.minElapsedMs;
            this.maxElapsedMs = builder.maxElapsedMs;
            this.totalOutputQueries = builder.totalOutputQueries;
            this.inputTokens = builder.inputTokens;
            this.outputTokens = builder.outputTokens;
            this.totalCost = builder.totalCost;
            this.errorCounts = builder.errorCounts;
            this.skipReasons = builder.skipReasons;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String stageName;
            private long executions;
            private long successes;
            private long failures;
            private long skipped;
            private double successRate;
            private double totalElapsedMs;
            private double averageElapsedMs;
            private long minElapsedMs;
            private long maxElapsedMs;
            private int totalOutputQueries;
            private double inputTokens;
            private double outputTokens;
            private double totalCost;
            private Map<String, Integer> errorCounts = new ConcurrentHashMap<>();
            private Map<String, Integer> skipReasons = new ConcurrentHashMap<>();
            
            public Builder stageName(String stageName) {
                this.stageName = stageName;
                return this;
            }
            
            public Builder executions(long executions) {
                this.executions = executions;
                return this;
            }
            
            public Builder successes(long successes) {
                this.successes = successes;
                return this;
            }
            
            public Builder failures(long failures) {
                this.failures = failures;
                return this;
            }
            
            public Builder skipped(long skipped) {
                this.skipped = skipped;
                return this;
            }
            
            public Builder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }
            
            public Builder totalElapsedMs(double totalElapsedMs) {
                this.totalElapsedMs = totalElapsedMs;
                return this;
            }
            
            public Builder averageElapsedMs(double averageElapsedMs) {
                this.averageElapsedMs = averageElapsedMs;
                return this;
            }
            
            public Builder minElapsedMs(long minElapsedMs) {
                this.minElapsedMs = minElapsedMs;
                return this;
            }
            
            public Builder maxElapsedMs(long maxElapsedMs) {
                this.maxElapsedMs = maxElapsedMs;
                return this;
            }
            
            public Builder totalOutputQueries(int totalOutputQueries) {
                this.totalOutputQueries = totalOutputQueries;
                return this;
            }
            
            public Builder inputTokens(double inputTokens) {
                this.inputTokens = inputTokens;
                return this;
            }
            
            public Builder outputTokens(double outputTokens) {
                this.outputTokens = outputTokens;
                return this;
            }
            
            public Builder totalCost(double totalCost) {
                this.totalCost = totalCost;
                return this;
            }
            
            public Builder errorCounts(Map<String, Integer> errorCounts) {
                this.errorCounts = errorCounts;
                return this;
            }
            
            public Builder skipReasons(Map<String, Integer> skipReasons) {
                this.skipReasons = skipReasons;
                return this;
            }
            
            public StageMetricsSummary build() {
                return new StageMetricsSummary(this);
            }
        }
    }
}