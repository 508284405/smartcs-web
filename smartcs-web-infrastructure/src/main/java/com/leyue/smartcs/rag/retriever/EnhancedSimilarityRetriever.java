package com.leyue.smartcs.rag.retriever;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 增强的相似性检索器，支持阈值过滤和重排序
 */
public class EnhancedSimilarityRetriever {
    
    private final VectorStore vectorStore;
    
    public EnhancedSimilarityRetriever(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    /**
     * 检索相似文档
     */
    public List<Document> retrieve(String query, int topK, float scoreThreshold, Map<String, Object> rerankingModel) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK).similarityThreshold(scoreThreshold).build();
        
        List<Document> results = vectorStore.similaritySearch(request);
        
        // 过滤分数低于阈值的文档
        List<Document> filtered = results.stream()
                .filter(doc -> {
                    float score = (float) doc.getMetadata().getOrDefault("score", 0.0f);
                    return score >= scoreThreshold;
                })
                .collect(Collectors.toList());
        
        // TODO: 实现重排序逻辑
        if (rerankingModel != null && !rerankingModel.isEmpty()) {
            // 这里可以集成重排序模型
            filtered = rerank(filtered, query, rerankingModel);
        }
        
        return filtered;
    }
    
    /**
     * 重排序逻辑（待实现）
     */
    private List<Document> rerank(List<Document> documents, String query, Map<String, Object> rerankingModel) {
        // TODO: 实现重排序逻辑
        return documents;
    }
} 