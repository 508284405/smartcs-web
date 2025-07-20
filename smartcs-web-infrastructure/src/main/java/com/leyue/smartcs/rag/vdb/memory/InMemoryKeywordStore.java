package com.leyue.smartcs.rag.vdb.memory;

import com.leyue.smartcs.rag.vdb.KeywordStore;
import dev.langchain4j.data.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryKeywordStore implements KeywordStore {
    
    private final Map<String, List<String>> store = new HashMap<>();
    
    @Override
    public void addTexts(List<Document> documents, List<String> keywordsList) {
        for (Document doc : documents) {
            String docId = doc.metadata().getString("doc_id");
            List<String> keywords = (keywordsList != null) ? keywordsList : extractKeywords(doc.text());
            store.put(docId, keywords);
        }
    }
    
    private List<String> extractKeywords(String content) {
        // 简单关键词提取逻辑，实际可使用更复杂算法
        List<String> keywords = new ArrayList<>();
        // TODO: 实现关键词提取
        return keywords;
    }
    
    @Override
    public void deleteByIds(List<String> nodeIds) {
        for (String id : nodeIds) {
            store.remove(id);
        }
    }
    
    @Override
    public void delete() {
        store.clear();
    }
} 