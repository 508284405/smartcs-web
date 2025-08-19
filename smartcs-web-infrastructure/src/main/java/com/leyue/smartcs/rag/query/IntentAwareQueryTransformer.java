package com.leyue.smartcs.rag.query;

import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 意图感知的查询转换器
 * 集成意图识别到RAG查询转换过程，根据用户意图智能优化查询策略
 * 
 * @author Claude
 */
@Slf4j
public class IntentAwareQueryTransformer implements QueryTransformer {
    
    private final ClassificationDomainService classificationDomainService;
    private final DynamicModelManager dynamicModelManager;
    private final Long modelId;
    private final int queryExpansionCount;
    private final boolean intentRecognitionEnabled;
    private final String defaultChannel;
    private final String defaultTenant;
    
    public IntentAwareQueryTransformer(ClassificationDomainService classificationDomainService,
                                       DynamicModelManager dynamicModelManager,
                                       Long modelId,
                                       int queryExpansionCount,
                                       boolean intentRecognitionEnabled,
                                       String defaultChannel,
                                       String defaultTenant) {
        this.classificationDomainService = classificationDomainService;
        this.dynamicModelManager = dynamicModelManager;
        this.modelId = modelId;
        this.queryExpansionCount = queryExpansionCount;
        this.intentRecognitionEnabled = intentRecognitionEnabled;
        this.defaultChannel = defaultChannel != null ? defaultChannel : "web";
        this.defaultTenant = defaultTenant != null ? defaultTenant : "default";
    }
    
    @Override
    public Collection<Query> transform(Query query) {
        try {
            log.debug("开始意图感知查询转换: query={}", query.text());
            
            if (!intentRecognitionEnabled) {
                // 如果意图识别未启用，直接使用基础查询扩展
                return performBasicQueryExpansion(query);
            }
            
            // 1. 执行意图识别
            IntentAnalysisResult intentResult = performIntentAnalysis(query.text());
            
            // 2. 基于意图优化查询
            Collection<Query> optimizedQueries = optimizeQueriesBasedOnIntent(query, intentResult);
            
            log.info("意图感知查询转换完成: originalQuery={}, intent={}, expandedCount={}", 
                    query.text(), intentResult.getIntentCode(), optimizedQueries.size());
            
            return optimizedQueries;
            
        } catch (Exception e) {
            log.error("意图感知查询转换失败，使用基础查询扩展: query={}", query.text(), e);
            return performBasicQueryExpansion(query);
        }
    }
    
    /**
     * 执行意图分析
     */
    private IntentAnalysisResult performIntentAnalysis(String queryText) {
        try {
            log.debug("执行意图分析: query={}", queryText);
            
            Map<String, Object> classificationResult = classificationDomainService.classifyUserInput(
                queryText, defaultChannel, defaultTenant);
            
            return IntentAnalysisResult.builder()
                .intentCode((String) classificationResult.get("intent_code"))
                .catalogCode((String) classificationResult.get("catalog_code"))
                .confidenceScore((Double) classificationResult.get("confidence_score"))
                .reasoning((String) classificationResult.get("reasoning"))
                .build();
                
        } catch (Exception e) {
            log.warn("意图分析失败，使用默认意图: query={}", queryText, e);
            return IntentAnalysisResult.createDefault();
        }
    }
    
    /**
     * 基于意图优化查询
     */
    private Collection<Query> optimizeQueriesBasedOnIntent(Query originalQuery, IntentAnalysisResult intentResult) {
        try {
            // 根据意图类型确定查询扩展策略
            QueryExpansionStrategy strategy = determineExpansionStrategy(intentResult);
            
            if (strategy.shouldSkipExpansion()) {
                // 某些意图不需要查询扩展（如问候语）
                return Collections.singletonList(originalQuery);
            }
            
            // 使用LLM进行智能查询扩展
            String expansionPrompt = buildExpansionPrompt(originalQuery.text(), intentResult, strategy);
            String expandedQueriesText = dynamicModelManager
                    .getChatModel(modelId)
                    .chat(UserMessage.from(expansionPrompt))
                    .aiMessage().text();
            
            // 解析扩展后的查询
            List<Query> expandedQueries = parseExpandedQueries(expandedQueriesText, strategy.getMaxQueries());
            
            // 确保至少包含原始查询
            if (expandedQueries.isEmpty()) {
                expandedQueries.add(originalQuery);
            }
            
            log.debug("查询扩展完成: intent={}, strategy={}, originalQuery={}, expandedCount={}", 
                    intentResult.getIntentCode(), strategy.getName(), originalQuery.text(), expandedQueries.size());
            
            return expandedQueries;
            
        } catch (Exception e) {
            log.warn("基于意图的查询优化失败，使用基础扩展: intent={}, query={}", 
                    intentResult.getIntentCode(), originalQuery.text(), e);
            return performBasicQueryExpansion(originalQuery);
        }
    }
    
    /**
     * 确定查询扩展策略
     */
    private QueryExpansionStrategy determineExpansionStrategy(IntentAnalysisResult intentResult) {
        String intentCode = intentResult.getIntentCode();
        double confidence = intentResult.getConfidenceScore();
        
        if ("greeting".equals(intentCode) || "goodbye".equals(intentCode)) {
            return QueryExpansionStrategy.NO_EXPANSION;
        } else if ("question".equals(intentCode) || "inquiry".equals(intentCode)) {
            return confidence > 0.7 ? QueryExpansionStrategy.DETAILED_EXPANSION : QueryExpansionStrategy.STANDARD_EXPANSION;
        } else if ("complaint".equals(intentCode)) {
            return QueryExpansionStrategy.PROBLEM_FOCUSED_EXPANSION;
        } else if ("technical_support".equals(intentCode)) {
            return QueryExpansionStrategy.TECHNICAL_EXPANSION;
        } else {
            return QueryExpansionStrategy.STANDARD_EXPANSION;
        }
    }
    
    /**
     * 构建查询扩展提示词
     */
    private String buildExpansionPrompt(String originalQuery, IntentAnalysisResult intentResult, QueryExpansionStrategy strategy) {
        return String.format("""
            你是一个智能查询优化助手。请根据用户的原始查询和识别的意图，生成%d个相关的查询变体。
            
            原始查询: %s
            识别意图: %s (置信度: %.2f)
            意图说明: %s
            
            %s
            
            请生成查询变体，每个查询占一行，不要包含编号或其他标记：
            """, 
            strategy.getMaxQueries(),
            originalQuery,
            intentResult.getIntentCode(),
            intentResult.getConfidenceScore(),
            intentResult.getReasoning(),
            strategy.getPromptGuidance()
        );
    }
    
    /**
     * 解析扩展后的查询
     */
    private List<Query> parseExpandedQueries(String expandedQueriesText, int maxQueries) {
        List<Query> queries = new ArrayList<>();
        
        if (expandedQueriesText == null || expandedQueriesText.trim().isEmpty()) {
            return queries;
        }
        
        String[] lines = expandedQueriesText.trim().split("\n");
        for (String line : lines) {
            String cleanedQuery = line.trim();
            // 移除可能的编号前缀
            cleanedQuery = cleanedQuery.replaceAll("^\\d+[.)\\s]+", "").trim();
            
            if (!cleanedQuery.isEmpty() && queries.size() < maxQueries) {
                queries.add(Query.from(cleanedQuery));
            }
        }
        
        return queries;
    }
    
    /**
     * 基础查询扩展（降级方案）
     */
    private Collection<Query> performBasicQueryExpansion(Query originalQuery) {
        try {
            if (queryExpansionCount <= 1) {
                return Collections.singletonList(originalQuery);
            }
            
            String prompt = String.format("""
                请为以下查询生成%d个相关的查询变体，每个查询占一行：
                
                原始查询: %s
                """, 
                queryExpansionCount - 1, // 减1是因为要包含原始查询
                originalQuery.text()
            );
            
            String expandedQueriesText = dynamicModelManager
                    .getChatModel(modelId)
                    .chat(UserMessage.from(prompt))
                    .aiMessage().text();
            List<Query> queries = parseExpandedQueries(expandedQueriesText, queryExpansionCount - 1);
            
            // 添加原始查询到开头
            queries.add(0, originalQuery);
            
            return queries;
            
        } catch (Exception e) {
            log.error("基础查询扩展失败: query={}", originalQuery.text(), e);
            return Collections.singletonList(originalQuery);
        }
    }
    
    /**
     * 意图分析结果
     */
    public static class IntentAnalysisResult {
        private final String intentCode;
        private final String catalogCode;
        private final Double confidenceScore;
        private final String reasoning;
        
        private IntentAnalysisResult(Builder builder) {
            this.intentCode = builder.intentCode;
            this.catalogCode = builder.catalogCode;
            this.confidenceScore = builder.confidenceScore;
            this.reasoning = builder.reasoning;
        }
        
        public static Builder builder() { return new Builder(); }
        
        public static IntentAnalysisResult createDefault() {
            return builder()
                .intentCode("UNKNOWN")
                .catalogCode("UNKNOWN")
                .confidenceScore(0.0)
                .reasoning("意图分析失败，使用默认处理")
                .build();
        }
        
        public String getIntentCode() { return intentCode; }
        public String getCatalogCode() { return catalogCode; }
        public Double getConfidenceScore() { return confidenceScore; }
        public String getReasoning() { return reasoning; }
        
        public static class Builder {
            private String intentCode;
            private String catalogCode;
            private Double confidenceScore;
            private String reasoning;
            
            public Builder intentCode(String intentCode) { this.intentCode = intentCode; return this; }
            public Builder catalogCode(String catalogCode) { this.catalogCode = catalogCode; return this; }
            public Builder confidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
            public Builder reasoning(String reasoning) { this.reasoning = reasoning; return this; }
            
            public IntentAnalysisResult build() { return new IntentAnalysisResult(this); }
        }
    }
    
    /**
     * 查询扩展策略
     */
    public enum QueryExpansionStrategy {
        NO_EXPANSION("无扩展", 1, "直接使用原始查询，无需扩展"),
        STANDARD_EXPANSION("标准扩展", 3, "生成标准的查询变体，保持查询的核心含义"),
        DETAILED_EXPANSION("详细扩展", 5, "生成更多详细的查询变体，从不同角度探索问题"),
        PROBLEM_FOCUSED_EXPANSION("问题聚焦扩展", 4, "专注于问题解决的查询变体，包含故障排除和解决方案相关的表述"),
        TECHNICAL_EXPANSION("技术扩展", 6, "生成技术相关的查询变体，包含专业术语和技术细节");
        
        private final String name;
        private final int maxQueries;
        private final String promptGuidance;
        
        QueryExpansionStrategy(String name, int maxQueries, String promptGuidance) {
            this.name = name;
            this.maxQueries = maxQueries;
            this.promptGuidance = promptGuidance;
        }
        
        public String getName() { return name; }
        public int getMaxQueries() { return maxQueries; }
        public String getPromptGuidance() { return promptGuidance; }
        public boolean shouldSkipExpansion() { return this == NO_EXPANSION; }
    }
}
