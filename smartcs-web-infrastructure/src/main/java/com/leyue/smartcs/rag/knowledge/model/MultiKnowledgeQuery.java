package com.leyue.smartcs.rag.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 多知识库查询参数
 * 支持跨多个知识库的联合查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiKnowledgeQuery {

    /**
     * 查询文本
     */
    private String queryText;

    /**
     * 知识库ID列表
     */
    private List<Long> knowledgeBaseIds;

    /**
     * 查询策略
     */
    private KnowledgeQuery.QueryStrategy strategy;

    /**
     * 每个知识库的最大返回结果数
     */
    private Integer maxResultsPerKnowledgeBase;

    /**
     * 总的最大返回结果数
     */
    private Integer totalMaxResults;

    /**
     * 最小相关性分数
     */
    private Double minScore;

    /**
     * 是否启用重排序
     */
    private Boolean enableRerank;

    /**
     * 结果合并策略
     */
    private MergeStrategy mergeStrategy;

    /**
     * 知识库权重配置
     */
    private Map<Long, Double> knowledgeBaseWeights;

    /**
     * 过滤条件
     */
    private Map<String, Object> filters;

    /**
     * 搜索参数
     */
    private Map<String, Object> searchParams;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 查询类型
     */
    private KnowledgeQuery.QueryType queryType;

    /**
     * 语言
     */
    private String language;

    /**
     * 并行查询配置
     */
    private ParallelQueryConfig parallelConfig;

    /**
     * 结果去重配置
     */
    private DeduplicationConfig deduplicationConfig;

    /**
     * 结果合并策略枚举
     */
    public enum MergeStrategy {
        SCORE_BASED,        // 基于分数合并
        ROUND_ROBIN,        // 轮询合并
        KNOWLEDGE_BASE_PRIORITY,  // 知识库优先级
        WEIGHTED_MERGE,     // 加权合并
        TIME_BASED,         // 基于时间合并
        RELEVANCE_FIRST     // 相关性优先
    }

    /**
     * 并行查询配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParallelQueryConfig {
        private Boolean enableParallel;
        private Integer maxConcurrentQueries;
        private Long queryTimeout;
        private Boolean failFast;
    }

    /**
     * 结果去重配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeduplicationConfig {
        private Boolean enableDeduplication;
        private DeduplicationMethod method;
        private Double similarityThreshold;
        private List<String> deduplicationFields;
    }

    /**
     * 去重方法枚举
     */
    public enum DeduplicationMethod {
        CONTENT_HASH,       // 内容哈希
        SEMANTIC_SIMILARITY, // 语义相似度
        EXACT_MATCH,        // 精确匹配
        FUZZY_MATCH         // 模糊匹配
    }
}