package com.leyue.smartcs.intent.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshotItem;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentClassificationGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.intent.ai.IntentClassificationAiService;
// Note: IntentClassificationLogService will be autowired by Spring from the app layer
import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 意图分类Gateway实现
 * 集成LangChain4j AI服务，提供基于大语言模型的意图分类能力
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentClassificationGatewayImpl implements IntentClassificationGateway {
    
    private final DynamicModelManager dynamicModelManager;
    private final IntentGateway intentGateway;
    private final IntentSnapshotGateway intentSnapshotGateway;
    private final IntentCatalogGateway intentCatalogGateway;
    private final ObjectMapper objectMapper;
    private final RedissonClient redissonClient;
    
    @Value("${smartcs.intent.classification.default-model-id:1}")
    private Long defaultModelId;
    
    @Value("${smartcs.intent.classification.confidence-threshold:0.6}")
    private Double defaultConfidenceThreshold;
    
    @Value("${smartcs.intent.classification.cache-ttl-minutes:60}")
    private Integer cacheTtlMinutes;
    
    @Value("${smartcs.intent.classification.max-retries:3}")
    private Integer maxRetries;
    
    @Value("${smartcs.intent.classification.timeout-ms:10000}")
    private Integer timeoutMs;
    
    @Value("${smartcs.intent.classification.enable-fallback:true}")
    private Boolean enableFallback;
    
    // 缓存AI服务实例
    private final Map<Long, IntentClassificationAiService> aiServiceCache = new ConcurrentHashMap<>();
    
    // 错误统计
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong modelErrors = new AtomicLong(0);
    private final AtomicLong timeoutErrors = new AtomicLong(0);
    private final AtomicLong parseErrors = new AtomicLong(0);
    
    @Override
    public Map<String, Object> classify(String text, Map<String, Object> context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取分类参数
            String channel = (String) context.get("channel");
            String tenant = (String) context.get("tenant");
            Long modelId = (Long) context.getOrDefault("model_id", defaultModelId);
            
            // 检查缓存
            String cacheKey = buildCacheKey(text, channel, tenant, modelId);
            Map<String, Object> cachedResult = getCachedResult(cacheKey);
            if (cachedResult != null) {
                cachedResult.put("from_cache", true);
                cachedResult.put("processing_time_ms", System.currentTimeMillis() - startTime);
                log.debug("从缓存获取分类结果: text={}, cacheKey={}", 
                        text.substring(0, Math.min(text.length(), 20)), cacheKey);
                return cachedResult;
            }
            
            // 获取AI服务实例
            IntentClassificationAiService aiService = getOrCreateAiService(modelId);
            
            // 构建意图列表
            String intentList = buildIntentList(channel, tenant);
            if (intentList.isEmpty()) {
                log.warn("没有找到可用的意图列表: channel={}, tenant={}", channel, tenant);
                return buildUnknownResult(text, channel, tenant, startTime);
            }
            
            // 调用AI分类（带重试机制）
            Map<String, Object> result = classifyWithRetry(aiService, text, intentList, channel, tenant, startTime);
            
            if (result == null) {
                log.warn("AI分类失败，使用回退机制: text={}", text.substring(0, Math.min(text.length(), 50)));
                result = handleClassificationFailure(text, channel, tenant, startTime, "AI_SERVICE_FAILED");
            }
            
            // 缓存结果（仅当置信度较高时缓存）
            Double confidenceScore = (Double) result.get("confidence_score");
            if (confidenceScore != null && confidenceScore >= defaultConfidenceThreshold) {
                cacheResult(cacheKey, result);
                log.debug("缓存分类结果: text={}, cacheKey={}, confidence={}", 
                        text.substring(0, Math.min(text.length(), 20)), cacheKey, confidenceScore);
            }
            
            // 记录详细的分类指标
            logClassificationMetrics(text, result);
            
            log.info("LLM分类完成: text={}, result={}, time={}ms", 
                    text.substring(0, Math.min(text.length(), 50)), 
                    result.get("intent_code"), 
                    result.get("processing_time_ms"));
            
            return result;
            
        } catch (Exception e) {
            log.error("意图分类失败: text={}, context={}", text, context, e);
            
            // 返回错误结果
            Map<String, Object> errorResult = buildUnknownResult(text, 
                    (String) context.get("channel"), 
                    (String) context.get("tenant"), 
                    startTime);
            errorResult.put("error", e.getMessage());
            errorResult.put("from_cache", false);
            return errorResult;
        }
    }
    
    @Override
    public Map<String, Map<String, Object>> batchClassify(String[] texts, Map<String, Object> context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取分类参数
            String channel = (String) context.get("channel");
            String tenant = (String) context.get("tenant");
            Long modelId = (Long) context.getOrDefault("model_id", defaultModelId);
            
            // 获取AI服务实例
            IntentClassificationAiService aiService = getOrCreateAiService(modelId);
            
            // 构建意图列表
            String intentList = buildIntentList(channel, tenant);
            if (intentList.isEmpty()) {
                log.warn("没有找到可用的意图列表: channel={}, tenant={}", channel, tenant);
                return buildBatchUnknownResults(texts, channel, tenant, startTime);
            }
            
            // 构建批量文本输入
            String textList = String.join("\n", texts);
            
            // 调用AI批量分类
            String aiResponse = aiService.classifyIntentsBatch(textList, intentList, channel, tenant);
            
            // 解析批量分类结果
            Map<String, Map<String, Object>> results = parseBatchClassificationResult(
                    aiResponse, texts, channel, tenant, startTime);
            
            // 记录批量分类指标
            logBatchClassificationMetrics(texts, results, System.currentTimeMillis() - startTime);
            
            log.info("LLM批量分类完成: count={}, time={}ms", 
                    texts.length, 
                    System.currentTimeMillis() - startTime);
            
            return results;
            
        } catch (Exception e) {
            log.error("批量意图分类失败: texts.length={}, context={}", texts.length, context, e);
            
            // 回退到单个分类
            Map<String, Map<String, Object>> results = new HashMap<>();
            for (int i = 0; i < texts.length; i++) {
                Map<String, Object> result = buildUnknownResult(texts[i], 
                        (String) context.get("channel"), 
                        (String) context.get("tenant"), 
                        startTime);
                result.put("error", e.getMessage());
                results.put("text_" + i, result);
            }
            return results;
        }
    }
    
    @Override
    public Map<String, Double> getThresholdSuggestion(Map<String, Object> samples) {
        // TODO: 基于样本数据和LLM分析计算阈值建议
        Map<String, Double> suggestions = new HashMap<>();
        suggestions.put("threshold_tau", defaultConfidenceThreshold);
        suggestions.put("margin_delta", 0.1);
        suggestions.put("temp_t", 1.0);
        
        return suggestions;
    }
    
    /**
     * 获取或创建AI服务实例
     */
    private IntentClassificationAiService getOrCreateAiService(Long modelId) {
        return aiServiceCache.computeIfAbsent(modelId, id -> {
            log.debug("创建IntentClassificationAiService实例: modelId={}", id);
            
            try {
                ChatModel chatModel = dynamicModelManager.getChatModel(id);
                return AiServices.builder(IntentClassificationAiService.class)
                        .chatModel(chatModel)
                        .build();
            } catch (Exception e) {
                log.error("创建IntentClassificationAiService失败: modelId={}", id, e);
                throw new RuntimeException("无法创建意图分类AI服务: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 构建意图列表字符串
     */
    private String buildIntentList(String channel, String tenant) {
        try {
            // 获取当前激活的意图快照
            var currentSnapshot = intentSnapshotGateway.getCurrentActiveSnapshot();
            if (currentSnapshot == null || currentSnapshot.getItems() == null || currentSnapshot.getItems().isEmpty()) {
                log.warn("没有找到激活的意图快照或快照为空: channel={}, tenant={}", channel, tenant);
                return getDefaultIntentList();
            }
            
            // 构建两级分类的意图列表
            StringBuilder intentListBuilder = new StringBuilder();
            
            // 按目录分组构建意图列表
            Map<String, List<IntentSnapshotItem>> catalogGrouped = currentSnapshot.getItems().stream()
                    .filter(item -> item.getIntentCode() != null && item.getIntentName() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            item -> getCatalogCodeByIntent(item.getIntentId())));
            
            for (Map.Entry<String, List<IntentSnapshotItem>> entry : catalogGrouped.entrySet()) {
                String catalogCode = entry.getKey();
                List<IntentSnapshotItem> intents = entry.getValue();
                
                // 构建目录级别的意图项
                IntentCatalog catalog = intentGateway.findById(intents.get(0).getIntentId()) != null ? 
                        intentCatalogGateway.findById(intentGateway.findById(intents.get(0).getIntentId()).getCatalogId()) : null;
                
                if (catalog != null) {
                    intentListBuilder.append(String.format("CATALOG_%s:%s\n", catalogCode, catalog.getName()));
                }
                
                // 构建具体意图项
                for (IntentSnapshotItem item : intents) {
                    intentListBuilder.append(String.format("%s:%s\n", item.getIntentCode(), item.getIntentName()));
                }
            }
            
            String result = intentListBuilder.toString().trim();
            log.debug("构建意图列表成功: channel={}, tenant={}, intentCount={}", 
                    channel, tenant, currentSnapshot.getItems().size());
            
            return result.isEmpty() ? getDefaultIntentList() : result;
            
        } catch (Exception e) {
            log.error("构建意图列表失败: channel={}, tenant={}", channel, tenant, e);
            return getDefaultIntentList();
        }
    }
    
    /**
     * 根据意图ID获取目录编码
     */
    private String getCatalogCodeByIntent(Long intentId) {
        try {
            Intent intent = intentGateway.findById(intentId);
            if (intent != null && intent.getCatalogId() != null) {
                IntentCatalog catalog = intentCatalogGateway.findById(intent.getCatalogId());
                return catalog != null ? catalog.getCode() : "UNKNOWN";
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.debug("获取意图目录编码失败: intentId={}", intentId, e);
            return "UNKNOWN";
        }
    }
    
    /**
     * 获取默认意图列表（当数据库中没有配置时使用）
     */
    private String getDefaultIntentList() {
        return "CATALOG_customer_service:客服服务\n" +
               "greeting:问候\n" +
               "goodbye:告别\n" +
               "CATALOG_business:业务咨询\n" +
               "question:询问\n" +
               "complaint:投诉\n" +
               "praise:表扬";
    }
    
    /**
     * 解析分类结果
     */
    private Map<String, Object> parseClassificationResult(String aiResponse, String text, 
                                                         String channel, String tenant, long startTime) {
        try {
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            
            Map<String, Object> result = new HashMap<>();
            result.put("intent_code", jsonNode.get("intentCode").asText());
            result.put("intent_name", jsonNode.has("intentName") ? jsonNode.get("intentName").asText() : null);
            result.put("catalog_code", jsonNode.has("catalogCode") ? jsonNode.get("catalogCode").asText() : null);
            result.put("catalog_name", jsonNode.has("catalogName") ? jsonNode.get("catalogName").asText() : null);
            result.put("confidence_score", jsonNode.get("confidenceScore").asDouble());
            result.put("catalog_confidence", jsonNode.has("catalogConfidence") ? jsonNode.get("catalogConfidence").asDouble() : null);
            result.put("intent_confidence", jsonNode.has("intentConfidence") ? jsonNode.get("intentConfidence").asDouble() : null);
            result.put("reason_code", jsonNode.has("reasonCode") ? jsonNode.get("reasonCode").asText() : null);
            result.put("reasoning", jsonNode.has("reasoning") ? jsonNode.get("reasoning").asText() : null);
            result.put("channel", channel);
            result.put("tenant", tenant);
            result.put("classification_level", "TWO_LEVEL");
            result.put("processing_time_ms", System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("解析LLM分类结果失败: response={}", aiResponse, e);
            return buildUnknownResult(text, channel, tenant, startTime);
        }
    }
    
    /**
     * 解析批量分类结果
     */
    private Map<String, Map<String, Object>> parseBatchClassificationResult(String aiResponse, String[] texts, 
                                                                           String channel, String tenant, long startTime) {
        try {
            JsonNode jsonArray = objectMapper.readTree(aiResponse);
            Map<String, Map<String, Object>> results = new HashMap<>();
            
            for (int i = 0; i < jsonArray.size() && i < texts.length; i++) {
                JsonNode item = jsonArray.get(i);
                Map<String, Object> result = new HashMap<>();
                
                result.put("intent_code", item.get("intentCode").asText());
                result.put("intent_name", item.has("intentName") ? item.get("intentName").asText() : null);
                result.put("catalog_code", item.has("catalogCode") ? item.get("catalogCode").asText() : null);
                result.put("catalog_name", item.has("catalogName") ? item.get("catalogName").asText() : null);
                result.put("confidence_score", item.get("confidenceScore").asDouble());
                result.put("catalog_confidence", item.has("catalogConfidence") ? item.get("catalogConfidence").asDouble() : null);
                result.put("intent_confidence", item.has("intentConfidence") ? item.get("intentConfidence").asDouble() : null);
                result.put("reason_code", item.has("reasonCode") ? item.get("reasonCode").asText() : null);
                result.put("reasoning", item.has("reasoning") ? item.get("reasoning").asText() : null);
                result.put("channel", channel);
                result.put("tenant", tenant);
                result.put("classification_level", "TWO_LEVEL");
                result.put("processing_time_ms", System.currentTimeMillis() - startTime);
                
                results.put("text_" + i, result);
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("解析LLM批量分类结果失败: response={}", aiResponse, e);
            return buildBatchUnknownResults(texts, channel, tenant, startTime);
        }
    }
    
    /**
     * 构建未知意图结果
     */
    private Map<String, Object> buildUnknownResult(String text, String channel, String tenant, long startTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("intent_code", "UNKNOWN");
        result.put("intent_name", "未知意图");
        result.put("confidence_score", 0.0);
        result.put("reason_code", "NO_MATCH");
        result.put("reasoning", "没有找到匹配的意图");
        result.put("channel", channel);
        result.put("tenant", tenant);
        result.put("processing_time_ms", System.currentTimeMillis() - startTime);
        return result;
    }
    
    /**
     * 构建批量未知意图结果
     */
    private Map<String, Map<String, Object>> buildBatchUnknownResults(String[] texts, String channel, String tenant, long startTime) {
        Map<String, Map<String, Object>> results = new HashMap<>();
        for (int i = 0; i < texts.length; i++) {
            results.put("text_" + i, buildUnknownResult(texts[i], channel, tenant, startTime));
        }
        return results;
    }
    
    /**
     * 构建缓存键
     */
    private String buildCacheKey(String text, String channel, String tenant, Long modelId) {
        try {
            String input = String.format("%s:%s:%s:%s", text.trim(), channel, tenant, modelId);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "intent:classification:" + sb.toString();
        } catch (Exception e) {
            log.warn("构建缓存键失败，使用简单哈希: {}", e.getMessage());
            return "intent:classification:" + Math.abs((text + channel + tenant + modelId).hashCode());
        }
    }
    
    /**
     * 获取缓存结果
     */
    private Map<String, Object> getCachedResult(String cacheKey) {
        try {
            RBucket<Map<String, Object>> bucket = redissonClient.getBucket(cacheKey);
            Map<String, Object> cached = bucket.get();
            return cached;
        } catch (Exception e) {
            log.debug("获取缓存失败: key={}, error={}", cacheKey, e.getMessage());
            return null;
        }
    }
    
    /**
     * 缓存分类结果
     */
    private void cacheResult(String cacheKey, Map<String, Object> result) {
        try {
            // 移除不需要缓存的临时字段
            Map<String, Object> cacheableResult = new HashMap<>(result);
            cacheableResult.remove("processing_time_ms");
            cacheableResult.remove("from_cache");
            
            RBucket<Map<String, Object>> bucket = redissonClient.getBucket(cacheKey);
            bucket.set(cacheableResult, Duration.ofMinutes(cacheTtlMinutes));
        } catch (Exception e) {
            log.debug("缓存分类结果失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }
    
    /**
     * 记录分类指标
     */
    private void logClassificationMetrics(String text, Map<String, Object> result) {
        try {
            String intentCode = (String) result.get("intent_code");
            String catalogCode = (String) result.get("catalog_code");
            Double confidenceScore = (Double) result.get("confidence_score");
            Double catalogConfidence = (Double) result.get("catalog_confidence");
            Double intentConfidence = (Double) result.get("intent_confidence");
            Boolean fromCache = (Boolean) result.get("from_cache");
            String reasonCode = (String) result.get("reason_code");
            Long processingTime = (Long) result.get("processing_time_ms");
            
            // 记录关键指标
            log.info("CLASSIFICATION_METRICS: text_length={}, intent_code={}, catalog_code={}, " +
                    "confidence={}, catalog_confidence={}, intent_confidence={}, " +
                    "from_cache={}, reason={}, processing_time={}ms",
                    text != null ? text.length() : 0,
                    intentCode,
                    catalogCode,
                    confidenceScore,
                    catalogConfidence,
                    intentConfidence,
                    fromCache,
                    reasonCode,
                    processingTime);
            
            // 性能告警
            if (processingTime != null && processingTime > 5000) {
                log.warn("PERFORMANCE_ALERT: 意图分类响应时间过长: {}ms, text_length={}, intent={}", 
                        processingTime, text != null ? text.length() : 0, intentCode);
            }
            
            // 置信度告警
            if (confidenceScore != null && confidenceScore < 0.3) {
                log.warn("CONFIDENCE_ALERT: 意图分类置信度过低: confidence={}, intent={}, text_preview={}", 
                        confidenceScore, intentCode, 
                        text != null ? text.substring(0, Math.min(text.length(), 50)) : "null");
            }
            
            // 未知意图统计
            if ("UNKNOWN".equals(intentCode)) {
                log.warn("UNKNOWN_INTENT: 未识别意图, text_preview={}, reason={}", 
                        text != null ? text.substring(0, Math.min(text.length(), 100)) : "null",
                        reasonCode);
            }
            
        } catch (Exception e) {
            log.debug("记录分类指标失败: {}", e.getMessage());
        }
    }
    
    /**
     * 记录批量分类指标
     */
    private void logBatchClassificationMetrics(String[] texts, Map<String, Map<String, Object>> results, long totalTime) {
        try {
            int totalCount = texts != null ? texts.length : 0;
            int processedCount = results != null ? results.size() : 0;
            int successCount = 0;
            int unknownCount = 0;
            double totalConfidence = 0.0;
            int cacheHits = 0;
            
            // 统计各项指标
            if (results != null) {
                for (Map<String, Object> result : results.values()) {
                    String intentCode = (String) result.get("intent_code");
                    Double confidence = (Double) result.get("confidence_score");
                    Boolean fromCache = (Boolean) result.get("from_cache");
                    
                    if (!"UNKNOWN".equals(intentCode)) {
                        successCount++;
                    } else {
                        unknownCount++;
                    }
                    
                    if (confidence != null) {
                        totalConfidence += confidence;
                    }
                    
                    if (Boolean.TRUE.equals(fromCache)) {
                        cacheHits++;
                    }
                }
            }
            
            double avgConfidence = processedCount > 0 ? totalConfidence / processedCount : 0.0;
            double successRate = totalCount > 0 ? (double) successCount / totalCount : 0.0;
            double cacheHitRate = totalCount > 0 ? (double) cacheHits / totalCount : 0.0;
            double avgTime = totalCount > 0 ? (double) totalTime / totalCount : 0.0;
            
            log.info("BATCH_CLASSIFICATION_METRICS: total={}, processed={}, success={}, unknown={}, " +
                    "success_rate={}, avg_confidence={}, cache_hit_rate={}, total_time={}ms, avg_time={}ms",
                    totalCount,
                    processedCount,
                    successCount,
                    unknownCount,
                    String.format("%.3f", successRate),
                    String.format("%.3f", avgConfidence),
                    String.format("%.3f", cacheHitRate),
                    totalTime,
                    String.format("%.1f", avgTime));
            
            // 批量处理性能告警
            if (avgTime > 2000) {
                log.warn("BATCH_PERFORMANCE_ALERT: 批量分类平均响应时间过长: avg_time={}ms, total_time={}ms, count={}", 
                        String.format("%.1f", avgTime), totalTime, totalCount);
            }
            
            // 批量成功率告警
            if (successRate < 0.8 && totalCount >= 5) {
                log.warn("BATCH_SUCCESS_ALERT: 批量分类成功率过低: success_rate={}, success={}/{}", 
                        String.format("%.3f", successRate), successCount, totalCount);
            }
            
        } catch (Exception e) {
            log.debug("记录批量分类指标失败: {}", e.getMessage());
        }
    }
    
    /**
     * 带重试机制的分类方法
     */
    private Map<String, Object> classifyWithRetry(IntentClassificationAiService aiService, String text, 
                                                 String intentList, String channel, String tenant, long startTime) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.debug("尝试AI分类: attempt={}/{}, text_length={}", attempt, maxRetries, text.length());
                
                // 执行分类
                String aiResponse = aiService.classifyIntent(text, intentList, channel, tenant);
                
                // 解析结果
                Map<String, Object> result = parseClassificationResult(aiResponse, text, channel, tenant, startTime);
                result.put("from_cache", false);
                result.put("retry_attempt", attempt);
                
                // 验证结果质量
                if (isValidClassificationResult(result)) {
                    if (attempt > 1) {
                        log.info("AI分类重试成功: attempt={}, text_length={}", attempt, text.length());
                    }
                    return result;
                } else {
                    log.warn("AI分类结果质量不佳，尝试重试: attempt={}, result={}", attempt, result.get("intent_code"));
                    parseErrors.incrementAndGet();
                    lastException = new RuntimeException("分类结果质量不佳: " + result.get("reason_code"));
                }
                
            } catch (Exception e) {
                lastException = e;
                modelErrors.incrementAndGet();
                
                if (isTimeoutException(e)) {
                    timeoutErrors.incrementAndGet();
                    log.warn("AI分类超时: attempt={}/{}, error={}", attempt, maxRetries, e.getMessage());
                } else {
                    log.warn("AI分类异常: attempt={}/{}, error={}", attempt, maxRetries, e.getMessage());
                }
                
                // 最后一次尝试失败，不再重试
                if (attempt == maxRetries) {
                    totalErrors.incrementAndGet();
                    log.error("AI分类最终失败: attempts={}, text_length={}", maxRetries, text.length(), e);
                    break;
                }
                
                // 重试前等待
                try {
                    Thread.sleep(Math.min(1000L * attempt, 3000L)); // 递增等待时间，最大3秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // 所有重试都失败了
        totalErrors.incrementAndGet();
        if (enableFallback) {
            log.info("启用回退机制进行规则分类");
            return performRuleBasedClassification(text, channel, tenant, startTime, lastException);
        }
        
        return null;
    }
    
    /**
     * 处理分类失败
     */
    private Map<String, Object> handleClassificationFailure(String text, String channel, String tenant, 
                                                           long startTime, String failureReason) {
        totalErrors.incrementAndGet();
        
        if (enableFallback) {
            log.info("执行回退分类策略: reason={}", failureReason);
            return performRuleBasedClassification(text, channel, tenant, startTime, 
                    new RuntimeException("分类失败: " + failureReason));
        } else {
            log.warn("回退机制已禁用，返回未知意图: reason={}", failureReason);
            Map<String, Object> result = buildUnknownResult(text, channel, tenant, startTime);
            result.put("failure_reason", failureReason);
            result.put("fallback_used", false);
            return result;
        }
    }
    
    /**
     * 基于规则的回退分类
     */
    private Map<String, Object> performRuleBasedClassification(String text, String channel, String tenant, 
                                                              long startTime, Exception originalException) {
        try {
            String textLower = text.toLowerCase().trim();
            Map<String, Object> result = new HashMap<>();
            
            // 简单的关键词规则分类
            if (textLower.matches(".*你好|hi|hello|早上好|下午好|晚上好.*")) {
                result.put("intent_code", "greeting");
                result.put("intent_name", "问候");
                result.put("catalog_code", "customer_service");
                result.put("catalog_name", "客服服务");
                result.put("confidence_score", 0.8);
                result.put("reason_code", "RULE_MATCH");
                result.put("reasoning", "基于关键词规则匹配的问候意图");
                
            } else if (textLower.matches(".*再见|bye|goodbye|拜拜.*")) {
                result.put("intent_code", "goodbye");
                result.put("intent_name", "告别");
                result.put("catalog_code", "customer_service");
                result.put("catalog_name", "客服服务");
                result.put("confidence_score", 0.8);
                result.put("reason_code", "RULE_MATCH");
                result.put("reasoning", "基于关键词规则匹配的告别意图");
                
            } else if (textLower.matches(".*投诉|complaint|不满|问题.*")) {
                result.put("intent_code", "complaint");
                result.put("intent_name", "投诉");
                result.put("catalog_code", "customer_service");
                result.put("catalog_name", "客服服务");
                result.put("confidence_score", 0.7);
                result.put("reason_code", "RULE_MATCH");
                result.put("reasoning", "基于关键词规则匹配的投诉意图");
                
            } else if (textLower.matches(".*怎么|如何|什么|为什么|?|？.*")) {
                result.put("intent_code", "question");
                result.put("intent_name", "询问");
                result.put("catalog_code", "business");
                result.put("catalog_name", "业务咨询");
                result.put("confidence_score", 0.6);
                result.put("reason_code", "RULE_MATCH");
                result.put("reasoning", "基于关键词规则匹配的询问意图");
                
            } else {
                result.put("intent_code", "UNKNOWN");
                result.put("intent_name", "未知意图");
                result.put("catalog_code", "UNKNOWN");
                result.put("catalog_name", "未知分类");
                result.put("confidence_score", 0.0);
                result.put("reason_code", "NO_RULE_MATCH");
                result.put("reasoning", "无法通过规则匹配到合适的意图");
            }
            
            // 添加通用字段
            result.put("channel", channel);
            result.put("tenant", tenant);
            result.put("classification_level", "RULE_BASED");
            result.put("fallback_used", true);
            result.put("original_error", originalException != null ? originalException.getMessage() : "AI分类失败");
            result.put("processing_time_ms", System.currentTimeMillis() - startTime);
            result.put("from_cache", false);
            
            log.info("规则分类完成: text_length={}, intent={}, confidence={}", 
                    text.length(), result.get("intent_code"), result.get("confidence_score"));
            
            return result;
            
        } catch (Exception e) {
            log.error("规则分类也失败了，返回默认结果", e);
            Map<String, Object> fallbackResult = buildUnknownResult(text, channel, tenant, startTime);
            fallbackResult.put("fallback_used", true);
            fallbackResult.put("fallback_error", e.getMessage());
            return fallbackResult;
        }
    }
    
    /**
     * 验证分类结果质量
     */
    private boolean isValidClassificationResult(Map<String, Object> result) {
        if (result == null) return false;
        
        String intentCode = (String) result.get("intent_code");
        Double confidence = (Double) result.get("confidence_score");
        String reasonCode = (String) result.get("reason_code");
        
        // 基本字段检查
        if (intentCode == null || confidence == null) {
            return false;
        }
        
        // 置信度检查
        if (confidence < 0.0 || confidence > 1.0) {
            return false;
        }
        
        // 如果是未知意图，检查原因
        if ("UNKNOWN".equals(intentCode) && "NO_MATCH".equals(reasonCode)) {
            return true; // 明确的未匹配也是有效结果
        }
        
        // 已知意图需要合理的置信度
        if (!"UNKNOWN".equals(intentCode) && confidence >= 0.3) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断是否为超时异常
     */
    private boolean isTimeoutException(Exception e) {
        if (e == null) return false;
        
        String message = e.getMessage();
        String className = e.getClass().getSimpleName();
        
        return className.contains("Timeout") || 
               className.contains("TimeOut") ||
               (message != null && message.toLowerCase().contains("timeout"));
    }
    
    /**
     * 获取错误统计信息
     */
    public Map<String, Long> getErrorStats() {
        return Map.of(
            "total_errors", totalErrors.get(),
            "model_errors", modelErrors.get(), 
            "timeout_errors", timeoutErrors.get(),
            "parse_errors", parseErrors.get()
        );
    }
}