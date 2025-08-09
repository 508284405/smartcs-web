package com.leyue.smartcs.dto.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
     * 内容注入器配置
     */
    @Valid
    private ContentInjectorConfig contentInjector;

    /**
     * 嵌入存储配置
     */
    @Valid
    private EmbeddingStoreConfig embeddingStore;

    /**
     * SQL查询配置
     */
    @Valid
    private SqlQueryConfig sqlQuery;

    /**
     * 记忆配置
     */
    @Valid
    private MemoryConfig memory;

    /**
     * 系统提示配置
     */
    @Valid
    private SystemPromptConfig systemPrompt;

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

        /**
         * 评分模型ID
         * 用于重排序打分的模型，未指定时回退到会话级 modelId
         */
        private Long scoringModelId;
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

        /**
         * 提示模板
         * 用于查询转换的提示模板
         */
        private String promptTemplate;

        /**
         * 模型ID
         * 用于查询转换的语言模型，未指定时回退到会话级 modelId
         */
        private Long modelId;
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

        /**
         * 提示模板
         * 用于路由决策的提示模板
         */
        private String promptTemplate;

        /**
         * 检索器描述映射
         * 每个检索器的描述信息，用于路由决策
         */
        private Map<String, String> retrieverToDescription;

        /**
         * 模型ID
         * 用于路由决策的语言模型，未指定时回退到会话级 modelId
         */
        private Long modelId;
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

        /**
         * 模型ID
         * 用于知识库检索的语言模型，未指定时回退到会话级 modelId
         */
        private Long modelId;
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

    /**
     * 内容注入器配置
     * 用于配置内容注入器的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentInjectorConfig {
        
        /**
         * 提示模板
         * 用于内容注入的提示模板
         */
        private String promptTemplate;

        /**
         * 要包含的元数据键列表
         * 指定哪些元数据键应该包含在注入的内容中
         */
        private List<String> metadataKeysToInclude;
    }

    /**
     * 嵌入存储配置
     * 用于配置嵌入向量存储的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddingStoreConfig {
        
        /**
         * 最大结果数量
         */
        @Min(value = 1, message = "最大结果数量不能少于1")
        @Max(value = 100, message = "最大结果数量不能超过100")
        @Builder.Default
        private Integer maxResults = 10;

        /**
         * 最小分数
         * 只有达到此分数的结果才会被返回
         */
        @DecimalMin(value = "0.0", message = "最小分数不能小于0.0")
        @DecimalMax(value = "1.0", message = "最小分数不能大于1.0")
        @Builder.Default
        private Double minScore = 0.0;

        /**
         * 要包含的元数据键列表
         */
        private List<String> metadataKeysToInclude;
    }

    /**
     * SQL查询配置
     * 用于配置SQL查询检索器的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SqlQueryConfig {
        
        /**
         * 最大结果数量
         */
        @Min(value = 1, message = "SQL查询最大结果数量不能少于1")
        @Max(value = 1000, message = "SQL查询最大结果数量不能超过1000")
        @Builder.Default
        private Integer maxResults = 100;

        /**
         * 超时时间（秒）
         */
        @Min(value = 1, message = "SQL查询超时时间不能少于1秒")
        @Max(value = 300, message = "SQL查询超时时间不能超过300秒")
        @Builder.Default
        private Integer timeout = 30;

        /**
         * 安全设置
         * 用于SQL查询的安全相关配置
         */
        private SecuritySettings securitySettings;

        /**
         * SQL查询安全设置
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SecuritySettings {
            
            /**
             * 允许的表名列表
             */
            private List<String> allowedTables;

            /**
             * 禁止的关键字列表
             */
            private List<String> forbiddenKeywords;

            /**
             * 是否启用查询验证
             */
            @Builder.Default
            private Boolean enableQueryValidation = true;

            /**
             * 是否只允许SELECT语句
             */
            @Builder.Default
            private Boolean selectOnly = true;
        }
    }

    /**
     * 记忆配置
     * 用于配置聊天记忆的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryConfig {
        
        /**
         * 最大消息数量
         * 记忆中保存的最大消息数量
         */
        @Min(value = 1, message = "最大消息数量不能少于1")
        @Max(value = 1000, message = "最大消息数量不能超过1000")
        @Builder.Default
        private Integer maxMessages = 100;

        /**
         * 记忆类型
         * 指定使用的记忆类型
         */
        @NotBlank(message = "记忆类型不能为空")
        @Builder.Default
        private String memoryType = "sliding_window";
    }

    /**
     * 系统提示配置
     * 用于配置系统提示的参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemPromptConfig {
        
        /**
         * 系统提示内容
         */
        @NotBlank(message = "系统提示不能为空")
        private String systemPrompt;

        /**
         * 提示模板
         * 用于格式化系统提示的模板
         */
        private String promptTemplate;
    }

    /**
     * 获取内容注入器配置，如果为null则返回默认配置
     */
    public ContentInjectorConfig getContentInjectorOrDefault() {
        return contentInjector != null ? contentInjector : ContentInjectorConfig.builder().build();
    }

    /**
     * 获取嵌入存储配置，如果为null则返回默认配置
     */
    public EmbeddingStoreConfig getEmbeddingStoreOrDefault() {
        return embeddingStore != null ? embeddingStore : EmbeddingStoreConfig.builder().build();
    }

    /**
     * 获取SQL查询配置，如果为null则返回默认配置
     */
    public SqlQueryConfig getSqlQueryOrDefault() {
        return sqlQuery != null ? sqlQuery : SqlQueryConfig.builder().build();
    }

    /**
     * 获取记忆配置，如果为null则返回默认配置
     */
    public MemoryConfig getMemoryOrDefault() {
        return memory != null ? memory : MemoryConfig.builder().build();
    }

    /**
     * 获取系统提示配置，如果为null则返回默认配置
     */
    public SystemPromptConfig getSystemPromptOrDefault() {
        return systemPrompt != null ? systemPrompt : SystemPromptConfig.builder().build();
    }
}