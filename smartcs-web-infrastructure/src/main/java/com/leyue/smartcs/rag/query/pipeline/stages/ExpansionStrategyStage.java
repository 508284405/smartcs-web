package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.query.Query;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 检索增强策略阶段
 * 实现高级检索策略以提升召回率和鲁棒性，包括：
 * 1. 多路Query生成 - 基于原query生成多个等义近义问法
 * 2. Step-back提升 - 先抽象成主题问题，再具体检索
 * 3. HyDE策略 - 生成假设答案进行向量检索
 * 4. RAG-Fusion - 多查询结果融合和重排序
 * 5. 查询扩展 - 基于领域知识和上下文的查询扩展
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpansionStrategyStage implements QueryTransformerStage {
    
    private final DynamicModelManager dynamicModelManager;
    private final DictionaryService dictionaryService;
    
    // 查询扩展策略
    private static final Map<ExpansionStrategy, String> EXPANSION_PROMPTS = createExpansionPrompts();
    
    // 主题抽象模式
    private static final Map<Pattern, String> TOPIC_PATTERNS = createTopicPatterns();
    
    // 查询复杂度评估模式
    private static final Pattern COMPLEX_QUERY_PATTERN = Pattern.compile(
        ".*(和|或者|以及|同时|另外|对比|比较|分析|评估|总结|汇总).*"
    );
    
    @Override
    public String getName() {
        return "ExpansionStrategyStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableExpanding() &&
               context.getAttribute("enableExpansionStrategy") != Boolean.FALSE;
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过检索增强策略处理");
            return Collections.emptyList();
        }
        
        log.debug("开始检索增强策略处理: inputCount={}", queries.size());
        
        try {
            List<Query> expandedQueries = new ArrayList<>();
            QueryContext.ExpandingConfig config = getExpandingConfig(context);
            
            for (Query query : queries) {
                try {
                    ExpansionResult result = applyExpansionStrategies(context, query, config);
                    
                    // 合并所有扩展结果
                    expandedQueries.addAll(result.getAllExpandedQueries());
                    
                    // 存储扩展信息到上下文
                    storeExpansionInfoInContext(context, query.text(), result);
                    
                } catch (Exception e) {
                    log.warn("单个查询检索增强失败，保留原查询: query={}", query.text(), e);
                    expandedQueries.add(query);
                }
            }
            
            // 应用融合和重排序策略
            expandedQueries = applyFusionStrategy(expandedQueries, context);
            
            // 应用最终限制
            expandedQueries = applyFinalLimits(expandedQueries, config);
            
            log.debug("检索增强策略处理完成: inputCount={}, outputCount={}", 
                    queries.size(), expandedQueries.size());
            
            return expandedQueries;
            
        } catch (Exception e) {
            log.error("检索增强策略处理失败: inputCount={}", queries.size(), e);
            // 发生错误时返回原始查询，保证系统稳定性
            log.warn("扩展策略阶段异常，返回原始查询: {}", e.getMessage());
            return queries;
        }
    }
    
    /**
     * 应用扩展策略
     */
    private ExpansionResult applyExpansionStrategies(QueryContext context, Query query, 
                                                   QueryContext.ExpandingConfig config) {
        
        String originalText = query.text();
        var resultBuilder = ExpansionResult.builder()
                .originalQuery(originalText);
        
        // 评估查询复杂度，决定使用哪些策略
        boolean isComplexQuery = isComplexQuery(originalText);
        
        List<CompletableFuture<List<Query>>> expansionFutures = new ArrayList<>();
        
        try {
            // 0. 基于字典的查询扩展（优先级最高）
            CompletableFuture<List<Query>> dictionaryExpansionFuture = TracingSupport
                    .supplyAsync(() -> expandQueriesWithDictionary(context, originalText))
                    .orTimeout(5, TimeUnit.SECONDS);
            expansionFutures.add(dictionaryExpansionFuture);
            
            // 1. 多路Query生成
            if (config.getN() > 1) {
                CompletableFuture<List<Query>> multiQueryFuture = TracingSupport
                        .supplyAsync(() -> generateMultipleQueries(originalText, config))
                        .orTimeout(10, TimeUnit.SECONDS);
                expansionFutures.add(multiQueryFuture);
            }
            
            // 2. Step-back策略（对复杂查询）
            if (isComplexQuery) {
                CompletableFuture<List<Query>> stepBackFuture = TracingSupport
                        .supplyAsync(() -> generateStepBackQueries(originalText))
                        .orTimeout(8, TimeUnit.SECONDS);
                expansionFutures.add(stepBackFuture);
            }
            
            // 3. HyDE策略（对抽象查询）
            if (isAbstractQuery(originalText)) {
                CompletableFuture<List<Query>> hydeFuture = TracingSupport
                        .supplyAsync(() -> generateHyDEQueries(originalText))
                        .orTimeout(15, TimeUnit.SECONDS);
                expansionFutures.add(hydeFuture);
            }
            
            // 4. 基于上下文的扩展
            List<Query> contextExpandedQueries = expandBasedOnContext(context, originalText);
            
            // 等待异步任务完成
            List<List<Query>> allExpansions = expansionFutures.stream()
                    .map(future -> {
                        try {
                            return future.get(20, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            log.warn("查询扩展异步任务失败: {}", e.getMessage());
                            return new ArrayList<Query>();
                        }
                    })
                    .collect(Collectors.toList());
            
            // 收集所有扩展结果
            List<Query> multiQueries = allExpansions.size() > 0 ? allExpansions.get(0) : new ArrayList<>();
            List<Query> stepBackQueries = allExpansions.size() > 1 ? allExpansions.get(1) : new ArrayList<>();
            List<Query> hydeQueries = allExpansions.size() > 2 ? allExpansions.get(2) : new ArrayList<>();
            
            return resultBuilder
                    .multiQueries(multiQueries)
                    .stepBackQueries(stepBackQueries)
                    .hydeQueries(hydeQueries)
                    .contextExpandedQueries(contextExpandedQueries)
                    .build();
                    
        } catch (Exception e) {
            log.error("扩展策略应用失败: query={}", originalText, e);
            return resultBuilder
                    .multiQueries(Arrays.asList(query))
                    .build();
        }
    }
    
    /**
     * 基于字典的查询扩展
     */
    private List<Query> expandQueriesWithDictionary(QueryContext context, String originalQuery) {
        List<Query> expandedQueries = new ArrayList<>();
        
        try {
            // 获取domain配置
            String domain = context.getAttribute("domain");
            if (domain == null) {
                domain = "default";
            }
            
            // 获取扩展策略字典
            Map<String, String> expansionStrategies = dictionaryService.getExpansionStrategies(
                context.getTenant(), context.getChannel(), domain, context.getLocale()
            );
            
            String lowerQuery = originalQuery.toLowerCase();
            
            // 应用字典扩展策略
            for (Map.Entry<String, String> entry : expansionStrategies.entrySet()) {
                String pattern = entry.getKey();
                String expansionTemplate = entry.getValue();
                
                if (lowerQuery.contains(pattern.toLowerCase())) {
                    // 应用扩展模板
                    String expandedQuery = applyExpansionTemplate(originalQuery, pattern, expansionTemplate);
                    if (!expandedQuery.equals(originalQuery)) {
                        expandedQueries.add(Query.from(expandedQuery));
                        log.debug("字典扩展: {} -> {}", pattern, expandedQuery);
                    }
                }
            }
            
            // 如果没有找到匹配的扩展策略，返回原查询
            if (expandedQueries.isEmpty()) {
                expandedQueries.add(Query.from(originalQuery));
            }
            
            return expandedQueries;
            
        } catch (Exception e) {
            log.warn("字典查询扩展失败: {}", e.getMessage());
            return Arrays.asList(Query.from(originalQuery));
        }
    }
    
    /**
     * 应用扩展模板
     */
    private String applyExpansionTemplate(String originalQuery, String pattern, String template) {
        try {
            // 简单的模板替换，实际可以更复杂
            if (template.contains("{query}")) {
                return template.replace("{query}", originalQuery);
            } else if (template.contains("{pattern}")) {
                return template.replace("{pattern}", pattern);
            } else {
                // 如果模板不包含占位符，直接返回模板内容
                return template;
            }
        } catch (Exception e) {
            log.debug("扩展模板应用失败: template={}, error={}", template, e.getMessage());
            return originalQuery;
        }
    }
    
    /**
     * 生成多路查询
     */
    private List<Query> generateMultipleQueries(String originalQuery, QueryContext.ExpandingConfig config) {
        try {
            ChatModel chatModel = getChatModel();
            if (chatModel == null) {
                log.warn("ChatModel不可用，跳过多路查询生成");
                return Arrays.asList(Query.from(originalQuery));
            }
            
            String prompt = String.format(EXPANSION_PROMPTS.get(ExpansionStrategy.MULTI_QUERY), 
                    config.getN(), originalQuery);
            
            String response = chatModel.chat(UserMessage.from(prompt)).aiMessage().text();
            return parseGeneratedQueries(response);
            
        } catch (Exception e) {
            log.warn("多路查询生成失败: {}", e.getMessage());
            return Arrays.asList(Query.from(originalQuery));
        }
    }
    
    /**
     * 生成Step-back查询
     */
    private List<Query> generateStepBackQueries(String originalQuery) {
        try {
            ChatModel chatModel = getChatModel();
            if (chatModel == null) {
                log.warn("ChatModel不可用，跳过Step-back查询生成");
                return Arrays.asList(Query.from(originalQuery));
            }
            
            // 首先生成主题问题
            String topicPrompt = String.format(EXPANSION_PROMPTS.get(ExpansionStrategy.STEP_BACK), originalQuery);
            String topicResponse = chatModel.chat(UserMessage.from(topicPrompt)).aiMessage().text();
            
            // 基于主题生成具体查询
            String specificPrompt = String.format(
                "基于主题'%s'和原始问题'%s'，生成2-3个具体的检索查询，每行一个：", 
                topicResponse.trim(), originalQuery
            );
            String specificResponse = chatModel.chat(UserMessage.from(specificPrompt)).aiMessage().text();
            
            List<Query> stepBackQueries = parseGeneratedQueries(specificResponse);
            stepBackQueries.add(Query.from(topicResponse.trim())); // 添加主题查询
            
            return stepBackQueries;
            
        } catch (Exception e) {
            log.warn("Step-back查询生成失败: {}", e.getMessage());
            return Arrays.asList(Query.from(originalQuery));
        }
    }
    
    /**
     * 生成HyDE查询
     */
    private List<Query> generateHyDEQueries(String originalQuery) {
        try {
            ChatModel chatModel = getChatModel();
            if (chatModel == null) {
                log.warn("ChatModel不可用，跳过HyDE查询生成");
                return Arrays.asList(Query.from(originalQuery));
            }
            
            String prompt = String.format(EXPANSION_PROMPTS.get(ExpansionStrategy.HYDE), originalQuery);
            String hypotheticalAnswer = chatModel.chat(UserMessage.from(prompt)).aiMessage().text();
            
            // 从假设答案中提取关键信息作为查询
            List<Query> hydeQueries = new ArrayList<>();
            
            // 添加假设答案本身
            hydeQueries.add(Query.from(hypotheticalAnswer.trim()));
            
            // 从假设答案中提取关键句子
            String[] sentences = hypotheticalAnswer.split("[。！？.!?]");
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.length() > 10 && sentence.length() < 100) {
                    hydeQueries.add(Query.from(sentence));
                }
            }
            
            return hydeQueries.stream().limit(3).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.warn("HyDE查询生成失败: {}", e.getMessage());
            return Arrays.asList(Query.from(originalQuery));
        }
    }
    
    /**
     * 基于上下文扩展查询
     */
    private List<Query> expandBasedOnContext(QueryContext context, String originalQuery) {
        List<Query> contextQueries = new ArrayList<>();
        
        // 基于租户和渠道添加上下文
        String tenant = context.getTenant();
        String channel = context.getChannel();
        
        if (tenant != null && !tenant.equals("default")) {
            contextQueries.add(Query.from(originalQuery + " " + tenant));
        }
        
        if (channel != null && !channel.equals("web")) {
            contextQueries.add(Query.from(originalQuery + " " + channel));
        }
        
        // 基于历史意图统计添加相关查询
        @SuppressWarnings("unchecked")
        Map<String, Integer> intentStats = (Map<String, Integer>) context.getAttribute("intentStats");
        if (intentStats != null && !intentStats.isEmpty()) {
            String topIntent = intentStats.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            
            if (topIntent != null && !topIntent.equals("UNKNOWN")) {
                // 基于主要意图生成相关查询
                contextQueries.add(Query.from(originalQuery + " " + mapIntentToKeywords(topIntent)));
            }
        }
        
        return contextQueries;
    }
    
    /**
     * 应用融合策略
     */
    private List<Query> applyFusionStrategy(List<Query> queries, QueryContext context) {
        if (queries.size() <= 3) {
            return queries; // 查询数量较少，无需融合
        }
        
        // 实现简化的RAG-Fusion策略：去重 + 排序
        Map<String, Query> uniqueQueries = new LinkedHashMap<>();
        Map<String, Double> queryScores = new HashMap<>();
        
        for (Query query : queries) {
            String normalizedText = query.text().toLowerCase().trim();
            
            if (!uniqueQueries.containsKey(normalizedText)) {
                uniqueQueries.put(normalizedText, query);
                
                // 计算查询质量分数（基于长度、复杂度等）
                double score = calculateQueryScore(query.text());
                queryScores.put(normalizedText, score);
            }
        }
        
        // 按分数排序
        return uniqueQueries.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(
                    queryScores.get(e2.getKey()), 
                    queryScores.get(e1.getKey())
                ))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
    
    /**
     * 应用最终限制
     */
    private List<Query> applyFinalLimits(List<Query> queries, QueryContext.ExpandingConfig config) {
        int maxQueries = Math.min(config.getN() * 2, 10); // 最多不超过原配置的2倍或10个
        
        return queries.stream()
                .limit(maxQueries)
                .collect(Collectors.toList());
    }
    
    /**
     * 判断是否为复杂查询
     */
    private boolean isComplexQuery(String query) {
        return COMPLEX_QUERY_PATTERN.matcher(query).find() || 
               query.split("[，,]").length > 2 ||
               query.length() > 50;
    }
    
    /**
     * 判断是否为抽象查询
     */
    private boolean isAbstractQuery(String query) {
        String[] abstractKeywords = {"原理", "概念", "理论", "思想", "方法", "策略", "模式", "框架"};
        String lowerQuery = query.toLowerCase();
        
        return Arrays.stream(abstractKeywords)
                .anyMatch(lowerQuery::contains);
    }
    
    /**
     * 解析生成的查询
     */
    private List<Query> parseGeneratedQueries(String response) {
        List<Query> queries = new ArrayList<>();
        
        String[] lines = response.split("\\n");
        for (String line : lines) {
            line = line.trim();
            
            // 移除编号和特殊字符
            line = line.replaceFirst("^\\d+[.)\\s]*", "");
            line = line.replaceFirst("^[-*•]\\s*", "");
            
            if (!line.isEmpty() && line.length() > 3) {
                queries.add(Query.from(line));
            }
        }
        
        return queries;
    }
    
    /**
     * 计算查询质量分数
     */
    private double calculateQueryScore(String query) {
        double score = 0.0;
        
        // 长度因子（适中长度得分高）
        int length = query.length();
        if (length >= 10 && length <= 80) {
            score += 1.0;
        } else if (length > 80) {
            score += 0.5;
        }
        
        // 包含关键词加分
        String[] qualityKeywords = {"如何", "什么", "为什么", "怎么", "方法", "解决", "问题"};
        for (String keyword : qualityKeywords) {
            if (query.contains(keyword)) {
                score += 0.2;
            }
        }
        
        // 避免过于简单的查询
        if (query.split("\\s+").length >= 3) {
            score += 0.3;
        }
        
        return score;
    }
    
    /**
     * 将意图映射到关键词
     */
    private String mapIntentToKeywords(String intentCode) {
        Map<String, String> intentKeywords = new HashMap<>();
        
        intentKeywords.put("troubleshooting", "故障 问题 解决");
        intentKeywords.put("product_info", "产品 功能 特性");
        intentKeywords.put("installation", "安装 配置 部署");
        intentKeywords.put("consultation", "咨询 建议 推荐");
        
        return intentKeywords.getOrDefault(intentCode, "");
    }
    
    /**
     * 获取扩展配置
     */
    private QueryContext.ExpandingConfig getExpandingConfig(QueryContext context) {
        QueryContext.ExpandingConfig config = context.getPipelineConfig().getExpandingConfig();
        
        if (config == null) {
            config = QueryContext.ExpandingConfig.builder()
                    .n(3)
                    .temperature(0.7)
                    .build();
        }
        
        return config;
    }
    
    /**
     * 获取ChatModel实例
     * 通过DynamicModelManager动态获取，避免直接依赖注入
     */
    private ChatModel getChatModel() {
        try {
            // 使用默认模型ID，实际项目中应从配置获取
            Long defaultModelId = 1L;
            return dynamicModelManager.getChatModel(defaultModelId);
        } catch (Exception e) {
            log.warn("获取ChatModel失败，将导致LLM相关功能降级", e);
            return null;
        }
    }
    
    /**
     * 存储扩展信息到上下文
     */
    private void storeExpansionInfoInContext(QueryContext context, String originalQuery, ExpansionResult result) {
        String key = "expansion_" + originalQuery.hashCode();
        context.setAttribute(key, result);
        
        // 统计扩展效果
        @SuppressWarnings("unchecked")
        Map<String, Integer> expansionStats = (Map<String, Integer>) context.getAttribute("expansionStats");
        if (expansionStats == null) {
            expansionStats = new HashMap<>();
            context.setAttribute("expansionStats", expansionStats);
        }
        
        expansionStats.merge("total", 1, Integer::sum);
        expansionStats.merge("multiQueries", result.getMultiQueries().size(), Integer::sum);
        expansionStats.merge("stepBackQueries", result.getStepBackQueries().size(), Integer::sum);
        expansionStats.merge("hydeQueries", result.getHydeQueries().size(), Integer::sum);
        expansionStats.merge("contextQueries", result.getContextExpandedQueries().size(), Integer::sum);
    }
    
    // 静态初始化方法
    
    private static Map<ExpansionStrategy, String> createExpansionPrompts() {
        Map<ExpansionStrategy, String> prompts = new HashMap<>();
        
        prompts.put(ExpansionStrategy.MULTI_QUERY, """
            你是一个查询扩展专家。基于用户的原始查询，生成%d个语义相似但表达方式不同的查询变体。
            这些变体应该：
            1. 保持原始查询的核心意图
            2. 使用不同的词汇和表达方式
            3. 适合用于文档检索
            4. 每行一个查询，不要编号
            
            原始查询：%s
            
            生成的查询变体：
            """);
        
        prompts.put(ExpansionStrategy.STEP_BACK, """
            你是一个抽象思维专家。请分析以下具体查询，并提取出其背后的主题或核心概念。
            
            具体查询：%s
            
            请回答这个查询背后的主题是什么？用一个简洁的短语或句子回答：
            """);
        
        prompts.put(ExpansionStrategy.HYDE, """
            你是一个领域专家。请基于以下查询，生成一个详细、准确的假设性答案。
            这个答案应该：
            1. 包含相关的技术细节和关键信息
            2. 使用专业术语和概念
            3. 结构清晰，逻辑完整
            4. 长度适中（100-300字）
            
            查询：%s
            
            假设性答案：
            """);
        
        return prompts;
    }
    
    private static Map<Pattern, String> createTopicPatterns() {
        Map<Pattern, String> patterns = new HashMap<>();
        
        patterns.put(Pattern.compile("如何.*"), "方法和步骤");
        patterns.put(Pattern.compile("什么是.*"), "概念和定义");
        patterns.put(Pattern.compile("为什么.*"), "原因和机制");
        patterns.put(Pattern.compile(".*故障.*"), "故障排除");
        patterns.put(Pattern.compile(".*对比.*|.*比较.*"), "对比分析");
        patterns.put(Pattern.compile(".*配置.*|.*设置.*"), "配置管理");
        
        return patterns;
    }
    
    // 内部枚举和数据结构
    
    /**
     * 扩展策略枚举
     */
    private enum ExpansionStrategy {
        MULTI_QUERY,    // 多路查询
        STEP_BACK,      // Step-back抽象
        HYDE,           // 假设性文档嵌入
        CONTEXT_BASED   // 基于上下文
    }
    
    /**
     * 扩展结果
     */
    @Data
    @lombok.Builder
    public static class ExpansionResult {
        private String originalQuery;
        
        @lombok.Builder.Default
        private List<Query> multiQueries = new ArrayList<>();
        
        @lombok.Builder.Default
        private List<Query> stepBackQueries = new ArrayList<>();
        
        @lombok.Builder.Default
        private List<Query> hydeQueries = new ArrayList<>();
        
        @lombok.Builder.Default
        private List<Query> contextExpandedQueries = new ArrayList<>();
        
        /**
         * 获取所有扩展查询
         */
        public List<Query> getAllExpandedQueries() {
            List<Query> allQueries = new ArrayList<>();
            
            // 添加原查询
            allQueries.add(Query.from(originalQuery));
            
            // 添加各种扩展查询
            allQueries.addAll(multiQueries);
            allQueries.addAll(stepBackQueries);
            allQueries.addAll(hydeQueries);
            allQueries.addAll(contextExpandedQueries);
            
            return allQueries;
        }
        
        /**
         * 获取扩展统计信息
         */
        public Map<String, Integer> getExpansionStats() {
            Map<String, Integer> stats = new HashMap<>();
            stats.put("multiQueries", multiQueries.size());
            stats.put("stepBackQueries", stepBackQueries.size());
            stats.put("hydeQueries", hydeQueries.size());
            stats.put("contextQueries", contextExpandedQueries.size());
            stats.put("total", getAllExpandedQueries().size());
            return stats;
        }
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化检索增强策略阶段");
        context.setAttribute("expansionStats", new HashMap<String, Integer>());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 打印扩展统计
        @SuppressWarnings("unchecked")
        Map<String, Integer> expansionStats = (Map<String, Integer>) context.getAttribute("expansionStats");
        if (expansionStats != null && !expansionStats.isEmpty()) {
            log.info("本次查询扩展统计: {}", expansionStats);
        }
    }
}
