package com.leyue.smartcs.rag.transformer;

import com.leyue.smartcs.dto.app.RagComponentConfig;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * LangChain4j查询转换器简化实现
 * 基本的查询扩展功能，避免复杂依赖
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
public class LangChain4jQueryTransformerImpl implements QueryTransformer {
    
    private final RagComponentConfig.QueryTransformerConfig config;
    
    @Override
    public Collection<Query> transform(Query query) {
        try {
            log.debug("LangChain4j查询转换开始: query={}", query.text());
            
            // 简化实现：基于配置进行基本的查询处理
            Collection<Query> result = performBasicTransformation(query);
            
            log.info("LangChain4j查询转换完成: originalQuery={}, transformedCount={}", 
                    query.text(), result.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("LangChain4j查询转换失败: query={}", query.text(), e);
            
            // 降级处理：返回原始查询
            return java.util.Collections.singletonList(query);
        }
    }
    
    /**
     * 执行基本的查询转换
     */
    private Collection<Query> performBasicTransformation(Query query) {
        java.util.List<Query> results = new java.util.ArrayList<>();
        
        // 总是保留原始查询
        results.add(query);
        
        // 根据配置决定是否进行扩展
        if (config != null && config.getN() > 1) {
            String queryText = query.text();
            
            // 基本的查询变体生成
            if (queryText.contains("？") || queryText.contains("?")) {
                // 为疑问句创建陈述句版本
                String declarative = queryText.replace("？", "").replace("?", "").trim();
                if (!declarative.isEmpty()) {
                    results.add(Query.from(declarative));
                }
            }
            
            // 添加同义词变体或其他基本转换
            // 这里可以根据需要扩展更多逻辑
        }
        
        return results;
    }
    
    /**
     * 获取配置摘要（用于日志）
     */
    public String getConfigSummary() {
        if (config == null) {
            return "DefaultConfig";
        }
        
        return String.format("Config{n=%d, intentEnabled=%s, modelId=%s}", 
                           config.getN(), 
                           config.getIntentRecognitionEnabled(), 
                           config.getModelId());
    }
}