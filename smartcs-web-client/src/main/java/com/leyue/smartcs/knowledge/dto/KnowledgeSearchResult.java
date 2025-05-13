package com.leyue.smartcs.knowledge.dto;

import lombok.Data;

import java.util.List;

/**
 * 知识检索结果对象
 */
@Data
public class KnowledgeSearchResult {
    /**
     * 检索结果类型（FAQ/DOC）
     */
    private String resultType;
    
    /**
     * FAQ结果列表
     */
    private List<FaqDTO> faqResults;
    
    /**
     * 文档段落结果列表
     */
    private List<EmbeddingWithScore> embeddingResults;
    
    /**
     * 带相似度分数的向量结果
     */
    @Data
    public static class EmbeddingWithScore {
        /**
         * 向量DTO
         */
        private EmbeddingDTO embedding;
        
        /**
         * 相似度分数
         */
        private Float score;
        
        /**
         * 文档标题
         */
        private String docTitle;
    }
} 