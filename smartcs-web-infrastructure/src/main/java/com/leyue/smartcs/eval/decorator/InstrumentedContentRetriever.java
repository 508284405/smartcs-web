package com.leyue.smartcs.eval.decorator;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 内容检索器装饰器
 * 用于采集RAG检索阶段的指标数据
 */
@Slf4j
@RequiredArgsConstructor
public class InstrumentedContentRetriever implements ContentRetriever {
    
    private final ContentRetriever delegate;
    private final RagMetricsCollector metricsCollector;
    
    @Override
    public List<Content> retrieve(Query query) {
        long startTime = System.currentTimeMillis();
        String queryText = query.text();
        
        try {
            log.debug("开始检索内容: query={}", queryText);
            
            // 执行检索
            List<Content> contents = delegate.retrieve(query);
            
            // 采集指标
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordRetrieval(queryText, contents, duration);
            
            log.debug("检索完成: query={}, resultCount={}, duration={}ms", 
                    queryText, contents.size(), duration);
            
            return contents;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordRetrievalError(queryText, e, duration);
            log.error("检索失败: query={}, duration={}ms, error={}", 
                    queryText, duration, e.getMessage(), e);
            throw e;
        }
    }
}