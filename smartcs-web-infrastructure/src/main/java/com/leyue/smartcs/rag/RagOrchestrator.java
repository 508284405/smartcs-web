//package com.leyue.smartcs.rag;
//
//import com.leyue.smartcs.rag.retriever.EnhancedContentAggregator;
//import com.leyue.smartcs.rag.retriever.EnhancedContentInjector;
//import com.leyue.smartcs.rag.retriever.KnowledgeContentRetriever;
//import dev.langchain4j.data.message.ChatMessage;
//import dev.langchain4j.rag.content.Content;
//import dev.langchain4j.rag.query.Query;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
///**
// * RAG编排器
// * 统一管理RAG流程：查询转换 -> 内容检索 -> 内容聚合 -> 内容注入
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class RagOrchestrator {
//
//    private final KnowledgeContentRetriever knowledgeContentRetriever;
//    private final EnhancedContentAggregator contentAggregator;
//    private final EnhancedContentInjector contentInjector;
//
//    // RAG配置参数
//    private static final int DEFAULT_MAX_RESULTS = 5;
//    private static final double DEFAULT_MIN_SCORE = 0.7;
//    private static final long RETRIEVAL_TIMEOUT_SECONDS = 30;
//
//    /**
//     * 执行完整的RAG流程
//     *
//     * @param userQuery 用户查询
//     * @param messages 原始消息列表
//     * @param knowledgeBaseId 知识库ID，可选
//     * @return 增强后的消息列表
//     */
//    public List<ChatMessage> executeRag(String userQuery, List<ChatMessage> messages, Long knowledgeBaseId) {
//        try {
//            log.info("开始执行RAG流程: query={}, knowledgeBaseId={}", userQuery, knowledgeBaseId);
//            long startTime = System.currentTimeMillis();
//
//            Query query = Query.from(userQuery);
//
//            // 1. 内容检索
//            List<Content> retrievedContents = retrieveContent(query, knowledgeBaseId);
//            if (retrievedContents.isEmpty()) {
//                log.info("未检索到相关内容，跳过RAG增强: query={}", userQuery);
//                return messages;
//            }
//
//            // 2. 内容聚合
//            List<Content> aggregatedContents = contentAggregator.aggregate(Map.of(query, List.of(retrievedContents)));
//
//            // 3. 内容注入
//            List<ChatMessage> enhancedMessages = contentInjector.injectToMessages(aggregatedContents, messages);
//
//            long executionTime = System.currentTimeMillis() - startTime;
//            log.info("RAG流程执行完成: query={}, retrievedCount={}, aggregatedCount={}, executionTime={}ms",
//                    userQuery, retrievedContents.size(), aggregatedContents.size(), executionTime);
//
//            return enhancedMessages;
//
//        } catch (Exception e) {
//            log.error("RAG流程执行失败: query={}, error={}", userQuery, e.getMessage(), e);
//            return messages; // 失败时返回原始消息
//        }
//    }
//
//    /**
//     * 执行带参数的RAG流程
//     *
//     * @param userQuery 用户查询
//     * @param messages 原始消息列表
//     * @param ragConfig RAG配置
//     * @return 增强后的消息列表
//     */
//    public List<ChatMessage> executeRag(String userQuery, List<ChatMessage> messages, RagConfig ragConfig) {
//        try {
//            log.info("开始执行配置化RAG流程: query={}, config={}", userQuery, ragConfig);
//            long startTime = System.currentTimeMillis();
//
//            Query query = Query.from(userQuery);
//
//            // 1. 内容检索
//            List<Content> retrievedContents = retrieveContentWithConfig(query, ragConfig);
//            if (retrievedContents.isEmpty()) {
//                log.info("未检索到相关内容，跳过RAG增强: query={}", userQuery);
//                return messages;
//            }
//
//            // 2. 内容聚合
//            List<Content> aggregatedContents;
//            if (ragConfig.isEnableWeightedAggregation() && ragConfig.getWeights() != null) {
//                aggregatedContents = contentAggregator.aggregateWithWeights(query, retrievedContents, ragConfig.getWeights());
//            } else {
//                aggregatedContents = contentAggregator.aggregate(Map.of(query, List.of(retrievedContents)));
//            }
//
//            // 3. 内容注入
//            List<ChatMessage> enhancedMessages;
//            switch (ragConfig.getInjectionStrategy()) {
//                case STRUCTURED -> enhancedMessages = contentInjector.injectWithStructure(aggregatedContents, messages, query);
//                case USER_MESSAGE -> enhancedMessages = contentInjector.injectIntoUserMessage(aggregatedContents, messages, ragConfig.getInjectionTemplate());
//                case INTELLIGENT -> enhancedMessages = contentInjector.injectIntelligently(aggregatedContents, messages, query, ragConfig.getConfidenceThreshold());
//                default -> enhancedMessages = contentInjector.injectToMessages(aggregatedContents, messages);
//            }
//
//            long executionTime = System.currentTimeMillis() - startTime;
//            log.info("配置化RAG流程执行完成: query={}, retrievedCount={}, aggregatedCount={}, executionTime={}ms",
//                    userQuery, retrievedContents.size(), aggregatedContents.size(), executionTime);
//
//            return enhancedMessages;
//
//        } catch (Exception e) {
//            log.error("配置化RAG流程执行失败: query={}, error={}", userQuery, e.getMessage(), e);
//            return messages;
//        }
//    }
//
//    /**
//     * 异步执行RAG流程
//     *
//     * @param userQuery 用户查询
//     * @param messages 原始消息列表
//     * @param knowledgeBaseId 知识库ID
//     * @return 增强后的消息列表的Future
//     */
//    public CompletableFuture<List<ChatMessage>> executeRagAsync(String userQuery, List<ChatMessage> messages, Long knowledgeBaseId) {
//        return CompletableFuture.supplyAsync(() -> executeRag(userQuery, messages, knowledgeBaseId))
//                .orTimeout(RETRIEVAL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
//                .exceptionally(throwable -> {
//                    log.error("异步RAG流程执行失败: query={}, error={}", userQuery, throwable.getMessage(), throwable);
//                    return messages; // 失败时返回原始消息
//                });
//    }
//
//    /**
//     * 获取知识片段用于预览
//     *
//     * @param userQuery 用户查询
//     * @param knowledgeBaseId 知识库ID
//     * @return 相关知识片段
//     */
//    public List<Content> getKnowledgePreview(String userQuery, Long knowledgeBaseId) {
//        try {
//            log.debug("获取知识预览: query={}, knowledgeBaseId={}", userQuery, knowledgeBaseId);
//            Query query = Query.from(userQuery);
//            return retrieveContent(query, knowledgeBaseId);
//        } catch (Exception e) {
//            log.error("获取知识预览失败: query={}, error={}", userQuery, e.getMessage(), e);
//            return List.of();
//        }
//    }
//
//    /**
//     * 检索内容
//     */
//    private List<Content> retrieveContent(Query query, Long knowledgeBaseId) {
//        if (knowledgeBaseId != null) {
//            return knowledgeContentRetriever.retrieveByKnowledgeBase(
//                query, knowledgeBaseId, DEFAULT_MAX_RESULTS, DEFAULT_MIN_SCORE);
//        } else {
//            return knowledgeContentRetriever.retrieve(query, DEFAULT_MAX_RESULTS, DEFAULT_MIN_SCORE);
//        }
//    }
//
//    /**
//     * 使用配置检索内容
//     */
//    private List<Content> retrieveContentWithConfig(Query query, RagConfig config) {
//        if (config.getKnowledgeBaseId() != null) {
//            return knowledgeContentRetriever.retrieveByKnowledgeBase(
//                query, config.getKnowledgeBaseId(), config.getMaxResults(), config.getMinScore());
//        } else {
//            return knowledgeContentRetriever.retrieve(query, config.getMaxResults(), config.getMinScore());
//        }
//    }
//
//    /**
//     * RAG配置类
//     */
//    public static class RagConfig {
//        private Long knowledgeBaseId;
//        private int maxResults = DEFAULT_MAX_RESULTS;
//        private double minScore = DEFAULT_MIN_SCORE;
//        private boolean enableWeightedAggregation = false;
//        private List<Double> weights;
//        private InjectionStrategy injectionStrategy = InjectionStrategy.DEFAULT;
//        private String injectionTemplate;
//        private double confidenceThreshold = 0.8;
//        private Map<String, Object> metadata;
//
//        // Getters and setters
//        public Long getKnowledgeBaseId() { return knowledgeBaseId; }
//        public void setKnowledgeBaseId(Long knowledgeBaseId) { this.knowledgeBaseId = knowledgeBaseId; }
//
//        public int getMaxResults() { return maxResults; }
//        public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
//
//        public double getMinScore() { return minScore; }
//        public void setMinScore(double minScore) { this.minScore = minScore; }
//
//        public boolean isEnableWeightedAggregation() { return enableWeightedAggregation; }
//        public void setEnableWeightedAggregation(boolean enableWeightedAggregation) { this.enableWeightedAggregation = enableWeightedAggregation; }
//
//        public List<Double> getWeights() { return weights; }
//        public void setWeights(List<Double> weights) { this.weights = weights; }
//
//        public InjectionStrategy getInjectionStrategy() { return injectionStrategy; }
//        public void setInjectionStrategy(InjectionStrategy injectionStrategy) { this.injectionStrategy = injectionStrategy; }
//
//        public String getInjectionTemplate() { return injectionTemplate; }
//        public void setInjectionTemplate(String injectionTemplate) { this.injectionTemplate = injectionTemplate; }
//
//        public double getConfidenceThreshold() { return confidenceThreshold; }
//        public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
//
//        public Map<String, Object> getMetadata() { return metadata; }
//        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
//
//        @Override
//        public String toString() {
//            return String.format("RagConfig{knowledgeBaseId=%d, maxResults=%d, minScore=%.2f, strategy=%s}",
//                               knowledgeBaseId, maxResults, minScore, injectionStrategy);
//        }
//    }
//
//    /**
//     * 内容注入策略
//     */
//    public enum InjectionStrategy {
//        DEFAULT,        // 默认注入策略
//        STRUCTURED,     // 结构化注入
//        USER_MESSAGE,   // 注入到用户消息
//        INTELLIGENT     // 智能注入
//    }
//}