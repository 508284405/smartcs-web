package com.leyue.smartcs.rag.metrics;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 槽位填充指标收集器
 * 负责收集和统计槽位填充相关的指标数据
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlotFillingMetricsCollector {
    
    // 基础计数器
    private final AtomicLong totalQueriesProcessed = new AtomicLong(0);
    private final AtomicLong queriesWithSlotFilling = new AtomicLong(0);
    private final AtomicLong queriesRequiringClarification = new AtomicLong(0);
    private final AtomicLong successfulClarifications = new AtomicLong(0);
    private final AtomicLong timeoutClarifications = new AtomicLong(0);
    private final AtomicLong retrievalBlocked = new AtomicLong(0);
    
    // 按意图统计
    private final Map<String, IntentMetrics> intentMetrics = new ConcurrentHashMap<>();
    
    // 性能指标
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong maxProcessingTime = new AtomicLong(0);
    
    /**
     * 记录查询处理开始
     */
    public void recordQueryStart(String intentCode) {
        totalQueriesProcessed.incrementAndGet();
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            getOrCreateIntentMetrics(intentCode).queryCount.incrementAndGet();
        }
        log.debug("记录查询处理开始: intent={}, total={}", intentCode, totalQueriesProcessed.get());
    }
    
    /**
     * 记录槽位填充激活
     */
    public void recordSlotFillingActivated(String intentCode, int totalSlots, int missingSlots) {
        queriesWithSlotFilling.incrementAndGet();
        
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            IntentMetrics metrics = getOrCreateIntentMetrics(intentCode);
            metrics.slotFillingActivated.incrementAndGet();
            metrics.totalSlotsSum.addAndGet(totalSlots);
            metrics.missingSlotsSum.addAndGet(missingSlots);
        }
        
        log.debug("记录槽位填充激活: intent={}, total={}, missing={}", intentCode, totalSlots, missingSlots);
    }
    
    /**
     * 记录需要澄清
     */
    public void recordClarificationRequired(String intentCode, int missingSlots, int questionsGenerated) {
        queriesRequiringClarification.incrementAndGet();
        
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            IntentMetrics metrics = getOrCreateIntentMetrics(intentCode);
            metrics.clarificationRequired.incrementAndGet();
            metrics.questionsGeneratedSum.addAndGet(questionsGenerated);
        }
        
        log.info("记录需要澄清: intent={}, missing={}, questions={}", intentCode, missingSlots, questionsGenerated);
    }
    
    /**
     * 记录澄清成功完成
     */
    public void recordClarificationSuccess(String intentCode, int clarificationAttempts) {
        successfulClarifications.incrementAndGet();
        
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            IntentMetrics metrics = getOrCreateIntentMetrics(intentCode);
            metrics.clarificationSuccess.incrementAndGet();
            metrics.clarificationAttemptsSum.addAndGet(clarificationAttempts);
        }
        
        log.info("记录澄清成功: intent={}, attempts={}", intentCode, clarificationAttempts);
    }
    
    /**
     * 记录澄清超时
     */
    public void recordClarificationTimeout(String intentCode, int clarificationAttempts) {
        timeoutClarifications.incrementAndGet();
        
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            IntentMetrics metrics = getOrCreateIntentMetrics(intentCode);
            metrics.clarificationTimeout.incrementAndGet();
            metrics.clarificationAttemptsSum.addAndGet(clarificationAttempts);
        }
        
        log.warn("记录澄清超时: intent={}, attempts={}", intentCode, clarificationAttempts);
    }
    
    /**
     * 记录检索被阻断
     */
    public void recordRetrievalBlocked(String intentCode, String reason) {
        retrievalBlocked.incrementAndGet();
        
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            getOrCreateIntentMetrics(intentCode).retrievalBlocked.incrementAndGet();
        }
        
        log.info("记录检索阻断: intent={}, reason={}", intentCode, reason);
    }
    
    /**
     * 记录处理完成时间
     */
    public void recordProcessingTime(String intentCode, long processingTimeMs) {
        totalProcessingTime.addAndGet(processingTimeMs);
        
        // 更新最大处理时间
        long currentMax = maxProcessingTime.get();
        while (processingTimeMs > currentMax && !maxProcessingTime.compareAndSet(currentMax, processingTimeMs)) {
            currentMax = maxProcessingTime.get();
        }
        
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            IntentMetrics metrics = getOrCreateIntentMetrics(intentCode);
            metrics.totalProcessingTime.addAndGet(processingTimeMs);
            
            // 更新该意图的最大处理时间
            long intentCurrentMax = metrics.maxProcessingTime.get();
            while (processingTimeMs > intentCurrentMax && !metrics.maxProcessingTime.compareAndSet(intentCurrentMax, processingTimeMs)) {
                intentCurrentMax = metrics.maxProcessingTime.get();
            }
        }
        
        log.debug("记录处理时间: intent={}, time={}ms", intentCode, processingTimeMs);
    }
    
    /**
     * 获取或创建意图指标
     */
    private IntentMetrics getOrCreateIntentMetrics(String intentCode) {
        return intentMetrics.computeIfAbsent(intentCode, k -> new IntentMetrics());
    }
    
    /**
     * 获取汇总指标
     */
    public SlotFillingMetricsSummary getMetricsSummary() {
        SlotFillingMetricsSummary summary = new SlotFillingMetricsSummary();
        
        // 基础指标
        summary.totalQueriesProcessed = totalQueriesProcessed.get();
        summary.queriesWithSlotFilling = queriesWithSlotFilling.get();
        summary.queriesRequiringClarification = queriesRequiringClarification.get();
        summary.successfulClarifications = successfulClarifications.get();
        summary.timeoutClarifications = timeoutClarifications.get();
        summary.retrievalBlocked = retrievalBlocked.get();
        
        // 计算比率
        if (summary.totalQueriesProcessed > 0) {
            summary.slotFillingActivationRate = (double) summary.queriesWithSlotFilling / summary.totalQueriesProcessed;
            summary.clarificationRequiredRate = (double) summary.queriesRequiringClarification / summary.totalQueriesProcessed;
        }
        
        if (summary.queriesRequiringClarification > 0) {
            summary.clarificationSuccessRate = (double) summary.successfulClarifications / summary.queriesRequiringClarification;
        }
        
        // 性能指标
        summary.totalProcessingTime = totalProcessingTime.get();
        summary.maxProcessingTime = maxProcessingTime.get();
        if (summary.totalQueriesProcessed > 0) {
            summary.averageProcessingTime = (double) summary.totalProcessingTime / summary.totalQueriesProcessed;
        }
        
        return summary;
    }
    
    /**
     * 获取意图级别的指标
     */
    public Map<String, IntentMetrics> getIntentMetrics() {
        return new ConcurrentHashMap<>(intentMetrics);
    }
    
    /**
     * 重置所有指标
     */
    public void reset() {
        totalQueriesProcessed.set(0);
        queriesWithSlotFilling.set(0);
        queriesRequiringClarification.set(0);
        successfulClarifications.set(0);
        timeoutClarifications.set(0);
        retrievalBlocked.set(0);
        totalProcessingTime.set(0);
        maxProcessingTime.set(0);
        intentMetrics.clear();
        log.info("槽位填充指标已重置");
    }
    
    /**
     * 意图级别的指标
     */
    @Data
    public static class IntentMetrics {
        private final AtomicInteger queryCount = new AtomicInteger(0);
        private final AtomicInteger slotFillingActivated = new AtomicInteger(0);
        private final AtomicInteger clarificationRequired = new AtomicInteger(0);
        private final AtomicInteger clarificationSuccess = new AtomicInteger(0);
        private final AtomicInteger clarificationTimeout = new AtomicInteger(0);
        private final AtomicInteger retrievalBlocked = new AtomicInteger(0);
        private final AtomicInteger totalSlotsSum = new AtomicInteger(0);
        private final AtomicInteger missingSlotsSum = new AtomicInteger(0);
        private final AtomicInteger questionsGeneratedSum = new AtomicInteger(0);
        private final AtomicInteger clarificationAttemptsSum = new AtomicInteger(0);
        private final AtomicLong totalProcessingTime = new AtomicLong(0);
        private final AtomicLong maxProcessingTime = new AtomicLong(0);
        
        public double getAverageProcessingTime() {
            int count = queryCount.get();
            return count > 0 ? (double) totalProcessingTime.get() / count : 0.0;
        }
        
        public double getAverageTotalSlots() {
            int count = slotFillingActivated.get();
            return count > 0 ? (double) totalSlotsSum.get() / count : 0.0;
        }
        
        public double getAverageMissingSlots() {
            int count = slotFillingActivated.get();
            return count > 0 ? (double) missingSlotsSum.get() / count : 0.0;
        }
    }
    
    /**
     * 指标汇总
     */
    @Data
    public static class SlotFillingMetricsSummary {
        private long totalQueriesProcessed;
        private long queriesWithSlotFilling;
        private long queriesRequiringClarification;
        private long successfulClarifications;
        private long timeoutClarifications;
        private long retrievalBlocked;
        
        private double slotFillingActivationRate;
        private double clarificationRequiredRate;
        private double clarificationSuccessRate;
        
        private long totalProcessingTime;
        private long maxProcessingTime;
        private double averageProcessingTime;
    }
}