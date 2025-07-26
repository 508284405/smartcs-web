package com.leyue.smartcs.rag.processor;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 父子索引处理器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ParentChildIndexProcessor {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    /**
     * 处理父子索引
     */
    public void processParentChildIndex(List<Document> docs) {
        log.info("开始处理父子索引，文档数量: {}", docs.size());

        try {
            // 生成嵌入向量并存储
            for (Document doc : docs) {
                dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(doc.text()).content();
                TextSegment textSegment = TextSegment.from(doc.text(), doc.metadata());
                embeddingStore.add(embedding, textSegment);
            }

            log.info("父子索引处理完成");

        } catch (Exception e) {
            log.error("父子索引处理失败", e);
            throw new RuntimeException("父子索引处理失败", e);
        }
    }
} 