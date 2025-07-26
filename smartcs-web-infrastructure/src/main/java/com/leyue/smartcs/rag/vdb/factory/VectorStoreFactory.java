package com.leyue.smartcs.rag.vdb.factory;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 向量存储工厂
 */
@Component
public class VectorStoreFactory {

    @Qualifier("simpleEmbeddingStore")
    private EmbeddingStore<TextSegment> simpleEmbeddingStore;

    @Qualifier("milvusEmbeddingStore")
    private EmbeddingStore<TextSegment> milvusEmbeddingStore;

    @Qualifier("pgVectorEmbeddingStore")
    private EmbeddingStore<TextSegment> pgVectorEmbeddingStore;

    public EmbeddingStore<TextSegment> get(String datasetType) {
        switch (datasetType) {
            case "milvus":
                return milvusEmbeddingStore;
            case "pgvector":
                return pgVectorEmbeddingStore;
            default:
                return simpleEmbeddingStore;
        }
    }
} 