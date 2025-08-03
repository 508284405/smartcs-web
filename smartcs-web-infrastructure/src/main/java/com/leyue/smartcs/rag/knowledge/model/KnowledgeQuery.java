package com.leyue.smartcs.rag.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 知识查询参数
 * 封装知识库查询的所有参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeQuery {

    /**
     * 查询文本
     */
    private String queryText;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 查询策略
     */
    private QueryStrategy strategy;

    /**
     * 最大返回结果数
     */
    private Integer maxResults;

    /**
     * 最小相关性分数
     */
    private Double minScore;

    /**
     * 是否启用重排序
     */
    private Boolean enableRerank;

    /**
     * 过滤条件
     */
    private Map<String, Object> filters;

    /**
     * 搜索参数
     */
    private Map<String, Object> searchParams;

    /**
     * 用户ID（用于个性化搜索）
     */
    private String userId;

    /**
     * 会话ID（用于上下文相关搜索）
     */
    private String sessionId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 查询类型
     */
    private QueryType queryType;

    /**
     * 语言
     */
    private String language;

    /**
     * 查询扩展选项
     */
    private QueryExpansion expansion;

    /**
     * 查询策略枚举
     */
    public enum QueryStrategy {
        VECTOR_SIMILARITY,  // 向量相似度
        KEYWORD_MATCH,      // 关键词匹配
        HYBRID_SEARCH,      // 混合搜索
        SEMANTIC_SEARCH,    // 语义搜索
        FUZZY_SEARCH,       // 模糊搜索
        EXACT_MATCH         // 精确匹配
    }

    /**
     * 查询类型枚举
     */
    public enum QueryType {
        QUESTION,           // 问题
        COMMAND,            // 命令
        SEARCH,             // 搜索
        RECOMMENDATION,     // 推荐
        SIMILARITY          // 相似性
    }

    /**
     * 查询扩展选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryExpansion {
        private Boolean enableSynonyms;      // 启用同义词
        private Boolean enableStemming;      // 启用词干化
        private Boolean enableSpellCheck;    // 启用拼写检查
        private List<String> additionalTerms; // 额外的查询词
        private Double expansionWeight;      // 扩展权重
    }
}