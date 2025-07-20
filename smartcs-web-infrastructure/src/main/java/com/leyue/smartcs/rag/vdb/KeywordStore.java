package com.leyue.smartcs.rag.vdb;

import dev.langchain4j.data.document.Document;

import java.util.List;

/**
 * 关键词存储接口
 */
public interface KeywordStore {
    
    /**
     * 添加文本
     * @param documents 文档列表
     * @param keywordsList 关键词列表（可选）
     */
    void addTexts(List<Document> documents, List<String> keywordsList);
    
    /**
     * 添加文本（无关键词）
     * @param documents 文档列表
     */
    default void addTexts(List<Document> documents) {
        addTexts(documents, null);
    }
    
    /**
     * 根据ID删除
     * @param nodeIds 节点ID列表
     */
    void deleteByIds(List<String> nodeIds);
    
    /**
     * 删除所有
     */
    void delete();
} 