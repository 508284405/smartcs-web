package com.leyue.smartcs.intent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 意图分类日志服务
 * 用于记录分类指标、分析和监控
 * 
 * @author Claude
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntentClassificationLogService {
    
    // 分类统计计数器
    private final AtomicLong totalClassifications = new AtomicLong(0);
    private final AtomicLong successfulClassifications = new AtomicLong(0);
    private final AtomicLong failedClassifications = new AtomicLong(0);
    private final AtomicLong cachedClassifications = new AtomicLong(0);
    private final AtomicLong lowConfidenceClassifications = new AtomicLong(0);
    
    /**
     * 记录分类结果
     * @param text 输入文本
     * @param result 分类结果
     * @param processingTimeMs 处理时间
     */
    @Async
    public void logClassificationResult(String text, Map<String, Object> result, Long processingTimeMs) {
        try {
            totalClassifications.incrementAndGet();
            
            // 基础指标记录
            String intentCode = (String) result.get("intent_code");
            Double confidenceScore = (Double) result.get("confidence_score");
            Boolean fromCache = (Boolean) result.get("from_cache");
            String reasonCode = (String) result.get("reason_code");
            String channel = (String) result.get("channel");
            String tenant = (String) result.get("tenant");
            
            // 分类成功率统计
            if (!"UNKNOWN".equals(intentCode)) {
                successfulClassifications.incrementAndGet();
            } else {
                failedClassifications.incrementAndGet();
            }
            
            // 缓存命中率统计
            if (Boolean.TRUE.equals(fromCache)) {
                cachedClassifications.incrementAndGet();
            }
            
            // 低置信度统计
            if (confidenceScore != null && confidenceScore < 0.6) {
                lowConfidenceClassifications.incrementAndGet();
            }
            
            // 详细日志记录
            log.info("意图分类记录: text_length={}, intent={}, confidence={}, reason={}, " +
                    "processing_time={}ms, from_cache={}, channel={}, tenant={}", 
                    text != null ? text.length() : 0, 
                    intentCode,
                    confidenceScore, 
                    reasonCode,
                    processingTimeMs, 
                    fromCache,
                    channel, 
                    tenant);
            
            // 性能监控
            if (processingTimeMs != null) {
                if (processingTimeMs > 5000) {
                    log.warn("意图分类响应时间过长: {}ms, text_length={}, intent={}", 
                            processingTimeMs, text != null ? text.length() : 0, intentCode);
                }
            }
            
            // 低置信度预警
            if (confidenceScore != null && confidenceScore < 0.3) {
                log.warn("意图分类置信度过低: confidence={}, text={}, intent={}", 
                        confidenceScore, 
                        text != null ? text.substring(0, Math.min(text.length(), 100)) : "null",
                        intentCode);
            }
            
            // TODO: 可以在这里添加更多的监控指标，如：
            // - 发送到监控系统（如Prometheus、Micrometer）
            // - 记录到专门的分析数据库
            // - 触发异常告警
            
        } catch (Exception e) {
            log.error("记录分类日志失败", e);
        }
    }
    
    /**
     * 记录批量分类结果
     * @param texts 输入文本数组
     * @param results 分类结果映射
     * @param processingTimeMs 总处理时间
     */
    @Async
    public void logBatchClassificationResult(String[] texts, Map<String, Map<String, Object>> results, Long processingTimeMs) {
        try {
            log.info("批量意图分类记录: total_texts={}, processed_results={}, total_time={}ms, avg_time={}ms",
                    texts != null ? texts.length : 0,
                    results != null ? results.size() : 0,
                    processingTimeMs,
                    processingTimeMs != null && texts != null && texts.length > 0 ? 
                            processingTimeMs / texts.length : 0);
            
            // 记录每个单独的分类结果
            if (results != null && texts != null) {
                for (int i = 0; i < texts.length; i++) {
                    String key = "text_" + i;
                    Map<String, Object> result = results.get(key);
                    if (result != null) {
                        logClassificationResult(texts[i], result, 
                                processingTimeMs != null ? processingTimeMs / texts.length : null);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("记录批量分类日志失败", e);
        }
    }
    
    /**
     * 获取分类统计信息
     * @return 统计数据
     */
    public Map<String, Object> getClassificationStats() {
        long total = totalClassifications.get();
        long successful = successfulClassifications.get();
        long failed = failedClassifications.get();
        long cached = cachedClassifications.get();
        long lowConfidence = lowConfidenceClassifications.get();
        
        return Map.of(
                "total_classifications", total,
                "successful_classifications", successful,
                "failed_classifications", failed,
                "cached_classifications", cached,
                "low_confidence_classifications", lowConfidence,
                "success_rate", total > 0 ? (double) successful / total : 0.0,
                "cache_hit_rate", total > 0 ? (double) cached / total : 0.0,
                "low_confidence_rate", total > 0 ? (double) lowConfidence / total : 0.0
        );
    }
    
    /**
     * 重置统计计数器
     */
    public void resetStats() {
        totalClassifications.set(0);
        successfulClassifications.set(0);
        failedClassifications.set(0);
        cachedClassifications.set(0);
        lowConfidenceClassifications.set(0);
        log.info("意图分类统计计数器已重置");
    }
    
    /**
     * 记录分类错误
     * @param text 输入文本
     * @param error 错误信息
     * @param context 上下文信息
     */
    @Async
    public void logClassificationError(String text, String error, Map<String, Object> context) {
        try {
            failedClassifications.incrementAndGet();
            totalClassifications.incrementAndGet();
            
            log.error("意图分类错误: text_length={}, error={}, channel={}, tenant={}", 
                    text != null ? text.length() : 0,
                    error,
                    context != null ? context.get("channel") : null,
                    context != null ? context.get("tenant") : null);
            
        } catch (Exception e) {
            log.error("记录分类错误日志失败", e);
        }
    }
}