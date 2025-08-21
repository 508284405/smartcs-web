package com.leyue.smartcs.rag.transformer;

import com.leyue.smartcs.app.rag.service.QueryTransformationApplicationService;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * LangChain4j查询转换器实现
 * 纯技术实现，委托给Domain Service处理业务逻辑
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
public class LangChain4jQueryTransformerImpl implements QueryTransformer {
    
    private final QueryTransformationApplicationService applicationService;
    private final RagComponentConfig.QueryTransformerConfig config;
    
    @Override
    public Collection<Query> transform(Query query) {
        try {
            log.debug("LangChain4j查询转换开始: query={}", query.text());
            
            // 委托给Application Service处理
            Collection<String> transformedQueries = applicationService.transformQuery(query.text(), config);
            
            // 转换为LangChain4j的Query对象
            Collection<Query> result = transformedQueries.stream()
                    .map(Query::from)
                    .collect(Collectors.toList());
            
            log.info("LangChain4j查询转换完成: originalQuery={}, transformedCount={}", 
                    query.text(), result.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("LangChain4j查询转换失败: query={}", query.text(), e);
            
            // 降级处理：返回原始查询
            return Collection.of(query);
        }
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