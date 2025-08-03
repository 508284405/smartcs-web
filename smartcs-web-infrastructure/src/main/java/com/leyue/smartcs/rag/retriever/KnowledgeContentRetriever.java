package com.leyue.smartcs.rag.retriever;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库内容检索器
 * 基于LangChain4j ContentRetriever实现向量相似度检索
 */
@Component("knowledgeContentRetriever")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeContentRetriever implements ContentRetriever {

    @Qualifier("knowledgeEmbeddingStore")
    private final EmbeddingStore<TextSegment> knowledgeEmbeddingStore;
    
    private final EmbeddingModel embeddingModel;

    // 默认检索参数
    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final double DEFAULT_MIN_SCORE = 0.7;

    @Override
    public List<Content> retrieve(Query query) {
        try {
            String queryText = query.text();
            log.debug("开始检索相关内容: query={}", queryText);

            // 生成查询向量
            var queryEmbedding = embeddingModel.embed(queryText).content();

            // 从知识库中检索相似内容
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(DEFAULT_MAX_RESULTS)
                    .minScore(DEFAULT_MIN_SCORE)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = knowledgeEmbeddingStore.search(searchRequest).matches();

            // 转换为Content对象
            List<Content> contents = matches.stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    double score = match.score();
                    
                    log.debug("检索到相关内容: score={}, text={}", score, 
                             segment.text().substring(0, Math.min(segment.text().length(), 100)));
                    
                    return Content.from(segment);
                })
                .collect(Collectors.toList());

            log.info("内容检索完成: query={}, foundCount={}", queryText, contents.size());
            return contents;

        } catch (Exception e) {
            log.error("内容检索失败: query={}, error={}", query.text(), e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 带参数的检索方法
     * 
     * @param query 查询对象
     * @param maxResults 最大结果数
     * @param minScore 最小相似度分数
     * @return 检索到的内容列表
     */
    public List<Content> retrieve(Query query, int maxResults, double minScore) {
        try {
            String queryText = query.text();
            log.debug("开始检索相关内容: query={}, maxResults={}, minScore={}", 
                     queryText, maxResults, minScore);

            // 生成查询向量
            var queryEmbedding = embeddingModel.embed(queryText).content();

            // 从知识库中检索相似内容
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = knowledgeEmbeddingStore.search(searchRequest).matches();

            // 转换为Content对象，并过滤分数
            List<Content> contents = matches.stream()
                .filter(match -> match.score() >= minScore)
                .map(match -> {
                    TextSegment segment = match.embedded();
                    double score = match.score();
                    
                    log.debug("检索到相关内容: score={}, text={}", score, 
                             segment.text().substring(0, Math.min(segment.text().length(), 100)));
                    
                    return Content.from(segment);
                })
                .collect(Collectors.toList());

            log.info("内容检索完成: query={}, foundCount={}", queryText, contents.size());
            return contents;

        } catch (Exception e) {
            log.error("内容检索失败: query={}, error={}", query.text(), e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 根据知识库ID检索内容
     * 
     * @param query 查询对象
     * @param knowledgeBaseId 知识库ID
     * @param maxResults 最大结果数
     * @param minScore 最小相似度分数
     * @return 检索到的内容列表
     */
    public List<Content> retrieveByKnowledgeBase(Query query, Long knowledgeBaseId, 
                                               int maxResults, double minScore) {
        try {
            String queryText = query.text();
            log.debug("开始从指定知识库检索内容: query={}, knowledgeBaseId={}, maxResults={}, minScore={}", 
                     queryText, knowledgeBaseId, maxResults, minScore);

            // 生成查询向量
            var queryEmbedding = embeddingModel.embed(queryText).content();

            // 从知识库中检索相似内容
            // TODO: 这里需要支持按知识库ID过滤，可能需要在metadata中添加knowledgeBaseId
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = knowledgeEmbeddingStore.search(searchRequest).matches();

            // 转换为Content对象并过滤指定知识库的内容
            List<Content> contents = matches.stream()
                .filter(match -> match.score() >= minScore)
                .filter(match -> {
                    // 检查是否属于指定的知识库
                    TextSegment segment = match.embedded();
                    if (segment.metadata() != null && segment.metadata().toMap().containsKey("knowledgeBaseId")) {
                        Long segmentKbId = Long.valueOf(segment.metadata().toMap().get("knowledgeBaseId").toString());
                        return knowledgeBaseId.equals(segmentKbId);
                    }
                    return false;
                })
                .map(match -> {
                    TextSegment segment = match.embedded();
                    double score = match.score();
                    
                    log.debug("检索到相关内容: score={}, knowledgeBaseId={}, text={}", 
                             score, knowledgeBaseId,
                             segment.text().substring(0, Math.min(segment.text().length(), 100)));
                    
                    return Content.from(segment);
                })
                .collect(Collectors.toList());

            log.info("知识库内容检索完成: query={}, knowledgeBaseId={}, foundCount={}", 
                    queryText, knowledgeBaseId, contents.size());
            return contents;

        } catch (Exception e) {
            log.error("知识库内容检索失败: query={}, knowledgeBaseId={}, error={}", 
                     query.text(), knowledgeBaseId, e.getMessage(), e);
            return List.of();
        }
    }
}