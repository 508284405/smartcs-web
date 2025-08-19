package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 查询扩展阶段
 * 使用LLM对输入查询进行智能扩展，生成多个相关的查询变体
 * 支持自定义PromptTemplate和各种扩展策略
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
public class ExpandingStage implements QueryTransformerStage {
    
    /**
     * 聊天模型
     */
    private final ChatModel chatModel;
    
    /**
     * 扩展查询转换器实例（延迟初始化）
     */
    private ExpandingQueryTransformer expandingTransformer;
    
    @Override
    public String getName() {
        return "ExpandingStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableExpanding();
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过扩展处理");
            return Collections.emptyList();
        }
        
        log.debug("开始查询扩展处理: inputCount={}", queries.size());
        
        try {
            QueryContext.ExpandingConfig config = getExpandingConfig(context);
            List<Query> expandedQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    Collection<Query> expanded = expandSingleQuery(query, config, context);
                    expandedQueries.addAll(expanded);
                } catch (Exception e) {
                    log.warn("单个查询扩展失败，保留原查询: query={}", query.text(), e);
                    expandedQueries.add(query);
                }
            }
            
            // 去重和限制数量
            expandedQueries = postProcessExpandedQueries(expandedQueries, context);
            
            log.debug("查询扩展处理完成: inputCount={}, outputCount={}", 
                    queries.size(), expandedQueries.size());
            
            return expandedQueries;
            
        } catch (Exception e) {
            log.error("查询扩展处理失败: inputCount={}", queries.size(), e);
            throw new QueryTransformationException(getName(), "查询扩展处理失败", e, true);
        }
    }
    
    /**
     * 获取扩展配置
     */
    private QueryContext.ExpandingConfig getExpandingConfig(QueryContext context) {
        QueryContext.ExpandingConfig config = context.getPipelineConfig().getExpandingConfig();
        
        // 如果没有配置，使用默认配置
        if (config == null) {
            config = QueryContext.ExpandingConfig.builder().build();
        }
        
        return config;
    }
    
    /**
     * 扩展单个查询
     */
    private Collection<Query> expandSingleQuery(Query query, QueryContext.ExpandingConfig config, 
                                              QueryContext context) {
        
        // 检查预算和超时
        if (context.getBudgetControl().isTokensBudgetExceeded(config.getN() * 50)) {
            log.warn("Token预算不足，跳过查询扩展: query={}", query.text());
            return Collections.singletonList(query);
        }
        
        if (context.getTimeoutControl().isTimeout()) {
            log.warn("执行超时，跳过查询扩展: query={}", query.text());
            return Collections.singletonList(query);
        }
        
        try {
            // 获取或创建扩展查询转换器
            ExpandingQueryTransformer transformer = getOrCreateExpandingTransformer(config);
            
            log.debug("执行LLM查询扩展: query={}, n={}", query.text(), config.getN());
            
            // 使用LangChain4j的ExpandingQueryTransformer进行扩展
            Collection<Query> expandedQueries = transformer.transform(query);
            
            // 记录token消耗（估算）
            recordTokenConsumption(context, query, expandedQueries);
            
            return expandedQueries;
            
        } catch (Exception e) {
            log.warn("LLM查询扩展失败: query={}", query.text(), e);
            throw new QueryTransformationException(getName(), 
                    "LLM查询扩展失败: " + e.getMessage(), e, true);
        }
    }
    
    /**
     * 获取或创建扩展查询转换器
     */
    private ExpandingQueryTransformer getOrCreateExpandingTransformer(QueryContext.ExpandingConfig config) {
        if (expandingTransformer == null) {
            var builder = ExpandingQueryTransformer.builder()
                    .chatModel(chatModel)
                    .n(config.getN());
            
            // 如果有自定义提示模板，设置它
            if (config.getPromptTemplate() != null && !config.getPromptTemplate().trim().isEmpty()) {
                builder.promptTemplate(PromptTemplate.from(config.getPromptTemplate()));
            }
            
            expandingTransformer = builder.build();
        }
        
        return expandingTransformer;
    }
    
    
    /**
     * 后处理扩展后的查询
     */
    private List<Query> postProcessExpandedQueries(List<Query> queries, QueryContext context) {
        // 去重
        Set<String> seen = new HashSet<>();
        List<Query> uniqueQueries = new ArrayList<>();
        
        for (Query query : queries) {
            String normalizedText = query.text().trim().toLowerCase();
            if (seen.add(normalizedText)) {
                uniqueQueries.add(query);
            }
        }
        
        // 应用数量限制
        int maxQueries = context.getPipelineConfig().getMaxQueries();
        if (uniqueQueries.size() > maxQueries) {
            log.debug("应用查询数量限制: current={}, max={}", uniqueQueries.size(), maxQueries);
            uniqueQueries = uniqueQueries.subList(0, maxQueries);
        }
        
        return uniqueQueries;
    }
    
    /**
     * 记录token消耗（估算）
     */
    private void recordTokenConsumption(QueryContext context, Query inputQuery, Collection<Query> outputQueries) {
        try {
            // 简单的token估算：中文按字数，英文按单词数
            int inputTokens = estimateTokens(inputQuery.text());
            int outputTokens = outputQueries.stream()
                    .mapToInt(q -> estimateTokens(q.text()))
                    .sum();
            
            // 记录到预算控制
            context.getBudgetControl().recordTokensConsumption(inputTokens + outputTokens);
            
            // 记录到指标收集器
            if (context.getMetricsCollector() != null) {
                context.getMetricsCollector().recordTokensConsumption(getName(), inputTokens, outputTokens);
            }
            
            log.debug("记录token消耗: stage={}, inputTokens={}, outputTokens={}", 
                    getName(), inputTokens, outputTokens);
            
        } catch (Exception e) {
            log.warn("记录token消耗失败: {}", e.getMessage());
        }
    }
    
    /**
     * 估算token数量
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 粗略估算：中文字符按1个token，英文单词按1个token，标点按0.5个token
        long chineseChars = text.chars()
                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)
                .count();
        
        long englishWords = Arrays.stream(text.split("\\s+"))
                .filter(word -> word.matches(".*[a-zA-Z].*"))
                .count();
        
        long punctuation = text.chars()
                .filter(c -> Character.getType(c) == Character.OTHER_PUNCTUATION)
                .count();
        
        return (int) (chineseChars + englishWords + punctuation * 0.5);
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化查询扩展阶段: config={}", context.getPipelineConfig().getExpandingConfig());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 无需清理资源
    }
}