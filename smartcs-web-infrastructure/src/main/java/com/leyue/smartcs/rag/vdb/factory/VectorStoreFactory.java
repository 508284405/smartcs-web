package com.leyue.smartcs.rag.vdb.factory;

import com.leyue.smartcs.domain.rag.model.Dataset;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 向量存储工厂
 */
@Component
public class VectorStoreFactory {
    
    @Autowired
    @Qualifier("simpleVectorStore")
    private VectorStore simpleVectorStore;
    
    @Autowired
    @Qualifier("milvusVectorStore")
    private VectorStore milvusVectorStore;
    
    @Autowired
    @Qualifier("pgVectorStore")
    private VectorStore pgVectorStore;
    
    public VectorStore get(Dataset dataset) {
        switch (dataset.getIndexingTechnique()) {
            case "high_quality":
                return milvusVectorStore;  // 或 pg
            case "economy":
                return simpleVectorStore;
            default:
                return simpleVectorStore;
        }
    }
} 