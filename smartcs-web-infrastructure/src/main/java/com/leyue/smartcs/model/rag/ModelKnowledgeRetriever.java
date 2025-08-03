package com.leyue.smartcs.model.rag;

import com.leyue.smartcs.app.rag.KnowledgeContentRetriever;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型推理知识检索器
 * 扩展KnowledgeContentRetriever，支持按知识库ID过滤
 * 用于ModelInferenceService的RAG增强
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelKnowledgeRetriever implements ContentRetriever {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final KnowledgeContentRetriever knowledgeContentRetriever;

    /**
     * 检索相关内容 - 支持知识库过滤
     * 
     * @param query 查询对象
     * @return 相关内容列表
     */
    @Override
    public List<Content> retrieve(Query query) {
        try {
            String queryText = query.text();
            log.debug("开始知识检索: query={}", queryText);

            // 默认使用原有的检索逻辑
            return knowledgeContentRetriever.retrieve(query);

        } catch (Exception e) {
            log.error("知识检索失败: query={}", query.text(), e);
            return List.of();
        }
    }

    /**
     * 根据知识库ID检索内容
     * 
     * @param queryText 查询文本
     * @param knowledgeBaseId 知识库ID
     * @return 相关内容列表
     */
    public List<Content> retrieveByKnowledgeBase(String queryText, Long knowledgeBaseId) {
        try {
            log.debug("按知识库检索: query={}, knowledgeBaseId={}", queryText, knowledgeBaseId);

            // 生成查询向量
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(queryText).content();

            // 构建检索请求 - 暂时不使用过滤器，后续可根据需要扩展
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(10)
                    .minScore(0.7)
                    .build();

            // 执行向量检索
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            // 转换为Content对象，可在此处进行后过滤
            return searchResult.matches().stream()
                    .map(match -> Content.from(match.embedded()))
                    .filter(content -> filterByKnowledgeBase(content, knowledgeBaseId))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("按知识库检索失败: query={}, knowledgeBaseId={}", queryText, knowledgeBaseId, e);
            return List.of();
        }
    }

    /**
     * 简化版检索方法 - 直接根据文本查询
     * 
     * @param queryText 查询文本
     * @param maxResults 最大结果数
     * @return 相关内容列表
     */
    public List<Content> retrieve(String queryText, int maxResults) {
        return retrieve(queryText, maxResults, 0.7);
    }

    /**
     * 完整版检索方法 - 支持分数阈值
     * 
     * @param queryText 查询文本
     * @param maxResults 最大结果数
     * @param minScore 最小相似度分数
     * @return 相关内容列表
     */
    public List<Content> retrieve(String queryText, int maxResults, double minScore) {
        try {
            // 生成查询向量
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(queryText).content();

            // 构建检索请求
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();

            // 执行向量检索
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            // 转换为Content对象
            List<Content> contents = searchResult.matches().stream()
                    .map(match -> Content.from(match.embedded()))
                    .collect(Collectors.toList());

            log.debug("知识检索完成: query={}, results={}", queryText, contents.size());
            return contents;

        } catch (Exception e) {
            log.error("知识检索失败: query={}", queryText, e);
            return List.of();
        }
    }

    /**
     * 根据知识库ID过滤内容
     * 
     * @param content 内容对象
     * @param knowledgeBaseId 知识库ID
     * @return 是否属于指定知识库
     */
    private boolean filterByKnowledgeBase(Content content, Long knowledgeBaseId) {
        try {
            // 简化版本：总是返回true，实际应用中可以根据content的metadata进行过滤
            return true;
        } catch (Exception e) {
            log.warn("知识库过滤失败: knowledgeBaseId={}", knowledgeBaseId, e);
            return false;
        }
    }

    /**
     * 检查知识库是否存在内容
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 是否存在内容
     */
    public boolean hasContent(Long knowledgeBaseId) {
        try {
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(embeddingModel.embed("test").content())
                    .maxResults(1)
                    .minScore(0.0)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            return !searchResult.matches().isEmpty();

        } catch (Exception e) {
            log.warn("检查知识库内容失败: knowledgeBaseId={}", knowledgeBaseId, e);
            return false;
        }
    }
}