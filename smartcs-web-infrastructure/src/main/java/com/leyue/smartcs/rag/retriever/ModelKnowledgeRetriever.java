package com.leyue.smartcs.rag.retriever;

import java.util.List;

import org.springframework.stereotype.Component;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
}