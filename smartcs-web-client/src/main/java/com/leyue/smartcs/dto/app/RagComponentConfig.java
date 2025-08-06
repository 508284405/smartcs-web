package com.leyue.smartcs.dto.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG组件配置类
 * 用于自定义检索增强生成(RAG)中各组件的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagComponentConfig {

    /**
     * 内容聚合器配置
     */
    @Valid
    private ContentAggregatorConfig contentAggregator;

    /**
     * 查询转换器配置
     */
    @Valid
    private QueryTransformerConfig queryTransformer;

    /**
     * 查询路由器配置
     */
    @Valid
    private QueryRouterConfig queryRouter;

    /**
     * Web搜索配置
     */
    @Valid
    private WebSearchConfig webSearch;

    /**
     * 知识库搜索配置
     */
    @Valid
    private KnowledgeSearchConfig knowledgeSearch;

    /**
     * 内容聚合器配置
     * 用于配置重排序内容聚合器的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentAggregatorConfig {
        
        /**
         * 最大结果数量
         * 聚合后返回的最大内容片段数量
         */
        @Min(value = 1, message = "最大结果数量不能少于1")
        @Max(value = 50, message = "最大结果数量不能超过50")
        @Builder.Default
        private Integer maxResults = 5;

        /**
         * 最小相关性分数
         * 只有达到此分数的内容才会被包含在结果中
         */
        @DecimalMin(value = "0.0", message = "最小相关性分数不能小于0.0")
        @DecimalMax(value = "1.0", message = "最小相关性分数不能大于1.0")
        @Builder.Default
        private Double minScore = 0.5;
    }

    /**
     * 查询转换器配置
     * 用于配置查询扩展转换器的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryTransformerConfig {
        
        /**
         * 查询扩展数量
         * 将原始查询扩展为多少个变体查询
         */
        @Min(value = 1, message = "查询扩展数量不能少于1")
        @Max(value = 10, message = "查询扩展数量不能超过10")
        @Builder.Default
        private Integer n = 5;
    }

    /**
     * 查询路由器配置
     * 用于配置查询路由器的检索器参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryRouterConfig {
        
        /**
         * 是否启用知识库检索
         */
        @Builder.Default
        private Boolean enableKnowledgeRetrieval = true;

        /**
         * 是否启用Web搜索
         */
        @Builder.Default
        private Boolean enableWebSearch = true;

        /**
         * 是否启用SQL查询检索
         */
        @Builder.Default
        private Boolean enableSqlQuery = false;
    }

    /**
     * Web搜索配置
     * 用于配置Web内容检索器的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSearchConfig {
        
        /**
         * 最大搜索结果数量
         */
        @Min(value = 1, message = "Web搜索最大结果数量不能少于1")
        @Max(value = 50, message = "Web搜索最大结果数量不能超过50")
        @Builder.Default
        private Integer maxResults = 10;

        /**
         * 搜索超时时间（秒）
         */
        @Min(value = 1, message = "搜索超时时间不能少于1秒")
        @Max(value = 60, message = "搜索超时时间不能超过60秒")
        @Builder.Default
        private Integer timeout = 10;
    }

    /**
     * 知识库搜索配置
     * 用于配置嵌入式存储内容检索器的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSearchConfig {
        
        /**
         * 返回的最相关结果数量
         */
        @Min(value = 1, message = "topK不能少于1")
        @Max(value = 100, message = "topK不能超过100")
        @Builder.Default
        private Integer topK = 5;

        /**
         * 相关性分数阈值
         * 只有达到此分数的结果才会被返回
         */
        @DecimalMin(value = "0.0", message = "相关性分数阈值不能小于0.0")
        @DecimalMax(value = "1.0", message = "相关性分数阈值不能大于1.0")
        @Builder.Default
        private Double scoreThreshold = 0.7;
    }

    /**
     * 获取内容聚合器配置，如果为null则返回默认配置
     */
    public ContentAggregatorConfig getContentAggregatorOrDefault() {
        return contentAggregator != null ? contentAggregator : ContentAggregatorConfig.builder().build();
    }

    /**
     * 获取查询转换器配置，如果为null则返回默认配置
     */
    public QueryTransformerConfig getQueryTransformerOrDefault() {
        return queryTransformer != null ? queryTransformer : QueryTransformerConfig.builder().build();
    }

    /**
     * 获取查询路由器配置，如果为null则返回默认配置
     */
    public QueryRouterConfig getQueryRouterOrDefault() {
        return queryRouter != null ? queryRouter : QueryRouterConfig.builder().build();
    }

    /**
     * 获取Web搜索配置，如果为null则返回默认配置
     */
    public WebSearchConfig getWebSearchOrDefault() {
        return webSearch != null ? webSearch : WebSearchConfig.builder().build();
    }

    /**
     * 获取知识库搜索配置，如果为null则返回默认配置
     */
    public KnowledgeSearchConfig getKnowledgeSearchOrDefault() {
        return knowledgeSearch != null ? knowledgeSearch : KnowledgeSearchConfig.builder().build();
    }
}