package com.leyue.smartcs.rag.query.pipeline;

import dev.langchain4j.rag.query.Query;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

/**
 * 查询转换上下文
 * 包含查询转换管线执行过程中所需的所有上下文信息
 * 
 * @author Claude
 */
@Data
@Builder
public class QueryContext {
    
    /**
     * 原始查询
     */
    @NonNull
    private final Query originalQuery;
    
    /**
     * 语言区域设置
     */
    private final String locale;
    
    /**
     * 租户标识
     */
    private final String tenant;
    
    /**
     * 渠道标识
     */
    private final String channel;
    
    /**
     * 会话历史信息
     */
    private final Map<String, Object> chatHistory;
    
    /**
     * 预算控制参数
     */
    private final BudgetControl budgetControl;
    
    /**
     * 超时控制参数
     */
    private final TimeoutControl timeoutControl;
    
    /**
     * 指标收集器
     */
    private final MetricsCollector metricsCollector;
    
    /**
     * 扩展属性，用于在不同阶段间传递数据
     */
    private final Map<String, Object> attributes;
    
    /**
     * 管线配置
     */
    private final PipelineConfig pipelineConfig;
    
    /**
     * 预算控制参数
     */
    @Data
    @Builder
    public static class BudgetControl {
        /**
         * 最大tokens预算
         */
        private final Integer maxTokens;
        
        /**
         * 最大成本（美元）
         */
        private final Double maxCost;
        
        /**
         * 已消耗tokens
         */
        private int consumedTokens;
        
        /**
         * 已消耗成本
         */
        private double consumedCost;
        
        /**
         * 检查tokens预算是否超限
         */
        public boolean isTokensBudgetExceeded(int additionalTokens) {
            return maxTokens != null && (consumedTokens + additionalTokens) > maxTokens;
        }
        
        /**
         * 检查成本预算是否超限
         */
        public boolean isCostBudgetExceeded(double additionalCost) {
            return maxCost != null && (consumedCost + additionalCost) > maxCost;
        }
        
        /**
         * 记录tokens消耗
         */
        public void recordTokensConsumption(int tokens) {
            this.consumedTokens += tokens;
        }
        
        /**
         * 记录成本消耗
         */
        public void recordCostConsumption(double cost) {
            this.consumedCost += cost;
        }
    }
    
    /**
     * 超时控制参数
     */
    @Data
    @Builder
    public static class TimeoutControl {
        /**
         * 最大延迟时间（毫秒）
         */
        private final Long maxLatencyMs;
        
        /**
         * 当前阶段开始时间戳
         */
        private long stageStartTime;
        
        /**
         * 管线总开始时间戳
         */
        private final long pipelineStartTime;
        
        /**
         * 检查是否超时
         */
        public boolean isTimeout() {
            if (maxLatencyMs == null) {
                return false;
            }
            return (System.currentTimeMillis() - pipelineStartTime) > maxLatencyMs;
        }
        
        /**
         * 获取剩余时间
         */
        public long getRemainingTimeMs() {
            if (maxLatencyMs == null) {
                return Long.MAX_VALUE;
            }
            return maxLatencyMs - (System.currentTimeMillis() - pipelineStartTime);
        }
        
        /**
         * 标记阶段开始
         */
        public void markStageStart() {
            this.stageStartTime = System.currentTimeMillis();
        }
        
        /**
         * 获取当前阶段耗时
         */
        public long getCurrentStageElapsedMs() {
            return System.currentTimeMillis() - stageStartTime;
        }
    }
    
    /**
     * 指标收集器
     */
    public interface MetricsCollector {
        /**
         * 记录阶段执行开始
         */
        void recordStageStart(String stageName, int inputQueryCount);
        
        /**
         * 记录阶段执行完成
         */
        void recordStageComplete(String stageName, int outputQueryCount, long elapsedMs);
        
        /**
         * 记录阶段执行失败
         */
        void recordStageFailure(String stageName, Throwable error, long elapsedMs);
        
        /**
         * 记录阶段跳过
         */
        void recordStageSkipped(String stageName, String reason);
        
        /**
         * 记录tokens消耗
         */
        void recordTokensConsumption(String stageName, int inputTokens, int outputTokens);
        
        /**
         * 记录成本消耗
         */
        void recordCostConsumption(String stageName, double cost);
    }
    
    /**
     * 管线配置
     */
    @Data
    @Builder
    public static class PipelineConfig {
        /**
         * 是否启用扩展
         */
        @Builder.Default
        private boolean enableExpanding = true;
        
        /**
         * 是否启用标准化
         */
        @Builder.Default
        private boolean enableNormalization = true;
        
        /**
         * 是否启用意图识别
         */
        @Builder.Default
        private boolean enableIntentRecognition = false;
        
        /**
         * 最大查询数量
         */
        @Builder.Default
        private int maxQueries = 10;
        
        /**
         * 是否保留原始查询
         */
        @Builder.Default
        private boolean keepOriginal = true;
        
        /**
         * 去重阈值
         */
        @Builder.Default
        private double dedupThreshold = 0.85;
        
        /**
         * 降级策略
         */
        @Builder.Default
        private FallbackPolicy fallbackPolicy = FallbackPolicy.SKIP_STAGE;
        
        /**
         * 扩展配置
         */
        private ExpandingConfig expandingConfig;
        
        /**
         * 标准化配置
         */
        private NormalizationConfig normalizationConfig;
    }
    
    /**
     * 扩展配置
     */
    @Data
    @Builder
    public static class ExpandingConfig {
        /**
         * 扩展数量
         */
        @Builder.Default
        private int n = 3;
        
        /**
         * 提示模板
         */
        private String promptTemplate;
        
        /**
         * 温度参数
         */
        @Builder.Default
        private double temperature = 0.7;
    }
    
    /**
     * 标准化配置
     */
    @Data
    @Builder  
    public static class NormalizationConfig {
        /**
         * 是否去除停用词
         */
        @Builder.Default
        private boolean removeStopwords = false;
        
        /**
         * 最大查询长度
         */
        @Builder.Default
        private int maxQueryLength = 512;
        
        /**
         * 是否标准化大小写
         */
        @Builder.Default
        private boolean normalizeCase = true;
        
        /**
         * 是否清理多余空白
         */
        @Builder.Default
        private boolean cleanWhitespace = true;
    }
    
    /**
     * 降级策略
     */
    public enum FallbackPolicy {
        /**
         * 跳过当前阶段
         */
        SKIP_STAGE,
        
        /**
         * 使用基础扩展
         */
        USE_BASIC_EXPANSION,
        
        /**
         * 仅使用原始查询
         */
        ORIGINAL_QUERY_ONLY
    }
    
    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return attributes != null ? (T) attributes.get(key) : null;
    }
    
    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        if (attributes != null) {
            attributes.put(key, value);
        }
    }
}