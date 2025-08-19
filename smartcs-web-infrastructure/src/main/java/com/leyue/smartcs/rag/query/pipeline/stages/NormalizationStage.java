package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 查询标准化阶段
 * 负责对输入查询进行清洗和标准化处理，包括：
 * - 去除多余空白字符
 * - 清理编号和标点符号
 * - 大小写标准化
 * - 长度限制
 * - 可选的停用词处理
 * 
 * @author Claude
 */
@Slf4j
public class NormalizationStage implements QueryTransformerStage {
    
    /**
     * 编号清理模式
     */
    private static final Pattern NUMBERING_PATTERN = Pattern.compile("^\\d+[.)\\s]+");
    
    /**
     * 多余空白模式
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    
    /**
     * 标点符号清理模式（保留基本标点）
     */
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}&&[^\\-.,!?:;\"'()]]+");
    
    /**
     * 中文停用词集合（基础版本）
     */
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", 
        "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "个", "们", "这个",
        "那个", "什么", "怎么", "为什么", "哪里", "谁", "when", "where", "what", "how", "why", "who",
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by"
    );
    
    @Override
    public String getName() {
        return "NormalizationStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableNormalization();
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过标准化处理");
            return Collections.emptyList();
        }
        
        log.debug("开始查询标准化处理: inputCount={}", queries.size());
        
        try {
            QueryContext.NormalizationConfig config = getNormalizationConfig(context);
            List<Query> normalizedQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    Query normalizedQuery = normalizeQuery(query, config);
                    if (normalizedQuery != null && !isQueryEmpty(normalizedQuery.text())) {
                        normalizedQueries.add(normalizedQuery);
                    }
                } catch (Exception e) {
                    log.warn("单个查询标准化失败，跳过: query={}", query.text(), e);
                    // 失败时保留原始查询
                    normalizedQueries.add(query);
                }
            }
            
            // 去重处理
            normalizedQueries = removeDuplicates(normalizedQueries);
            
            log.debug("查询标准化处理完成: inputCount={}, outputCount={}", 
                    queries.size(), normalizedQueries.size());
            
            return normalizedQueries;
            
        } catch (Exception e) {
            log.error("查询标准化处理失败: inputCount={}", queries.size(), e);
            throw new QueryTransformationException(getName(), "查询标准化处理失败", e, true);
        }
    }
    
    /**
     * 获取标准化配置
     */
    private QueryContext.NormalizationConfig getNormalizationConfig(QueryContext context) {
        QueryContext.NormalizationConfig config = context.getPipelineConfig().getNormalizationConfig();
        
        // 如果没有配置，使用默认配置
        if (config == null) {
            config = QueryContext.NormalizationConfig.builder().build();
        }
        
        return config;
    }
    
    /**
     * 标准化单个查询
     */
    private Query normalizeQuery(Query query, QueryContext.NormalizationConfig config) {
        String text = query.text();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // 1. 去除编号前缀
        text = NUMBERING_PATTERN.matcher(text).replaceFirst("");
        
        // 2. 清理多余空白
        if (config.isCleanWhitespace()) {
            text = WHITESPACE_PATTERN.matcher(text.trim()).replaceAll(" ");
        }
        
        // 3. 大小写标准化
        if (config.isNormalizeCase()) {
            // 对于中文查询，保持原始大小写；对于英文，转换为小写
            if (containsChinese(text)) {
                // 中文查询保持原样
            } else {
                text = text.toLowerCase();
            }
        }
        
        // 4. 清理部分标点符号（保留常用标点）
        text = PUNCTUATION_PATTERN.matcher(text).replaceAll(" ");
        text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ").trim();
        
        // 5. 应用长度限制
        if (text.length() > config.getMaxQueryLength()) {
            text = text.substring(0, config.getMaxQueryLength()).trim();
            log.debug("查询长度超限，已截断: originalLength={}, maxLength={}", 
                    query.text().length(), config.getMaxQueryLength());
        }
        
        // 6. 可选的停用词处理
        if (config.isRemoveStopwords()) {
            text = removeStopwords(text);
        }
        
        // 7. 最终清理
        text = text.trim();
        
        return isQueryEmpty(text) ? null : Query.from(text);
    }
    
    /**
     * 检查文本是否包含中文字符
     */
    private boolean containsChinese(String text) {
        return text.chars().anyMatch(c -> 
            Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN);
    }
    
    /**
     * 移除停用词
     */
    private String removeStopwords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // 简单的词分割（基于空格和常见分隔符）
        String[] words = text.split("[\\s\\p{Punct}]+");
        
        List<String> filteredWords = Arrays.stream(words)
                .map(String::trim)
                .filter(word -> !word.isEmpty() && !STOP_WORDS.contains(word.toLowerCase()))
                .collect(Collectors.toList());
        
        // 如果过滤后为空，返回原文
        if (filteredWords.isEmpty()) {
            return text;
        }
        
        return String.join(" ", filteredWords);
    }
    
    /**
     * 检查查询是否为空或无效
     */
    private boolean isQueryEmpty(String text) {
        return text == null || text.trim().isEmpty() || text.trim().length() < 2;
    }
    
    /**
     * 去重处理
     */
    private List<Query> removeDuplicates(List<Query> queries) {
        Set<String> seen = new HashSet<>();
        List<Query> uniqueQueries = new ArrayList<>();
        
        for (Query query : queries) {
            String normalizedText = query.text().trim().toLowerCase();
            if (seen.add(normalizedText)) {
                uniqueQueries.add(query);
            }
        }
        
        if (uniqueQueries.size() != queries.size()) {
            log.debug("去重处理: 原数量={}, 去重后={}", queries.size(), uniqueQueries.size());
        }
        
        return uniqueQueries;
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化查询标准化阶段: config={}", context.getPipelineConfig().getNormalizationConfig());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 无需清理资源
    }
}