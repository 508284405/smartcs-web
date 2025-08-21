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
     * 用于配置查询扩展转换器的参数，支持管线化处理
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

        /**
         * 是否启用意图识别
         * 控制是否在查询转换过程中集成意图识别功能
         */
        @Builder.Default
        private Boolean intentRecognitionEnabled = false;

        /**
         * 默认渠道
         * 意图识别使用的默认渠道标识
         */
        @Builder.Default
        private String defaultChannel = "web";

        /**
         * 默认租户
         * 意图识别使用的默认租户标识
         */
        @Builder.Default
        private String defaultTenant = "default";
        
        // ========== 管线化配置项 ==========
        
        /**
         * 是否启用管线化处理
         * 启用后使用QueryTransformerPipeline，否则回退到传统的IntentAwareQueryTransformer
         */
        @Builder.Default
        private Boolean enablePipeline = false;
        
        /**
         * 是否启用标准化阶段
         */
        @Builder.Default
        private Boolean enableNormalization = true;
        
        /**
         * 是否启用扩展阶段
         */
        @Builder.Default
        private Boolean enableExpanding = true;
        
        /**
         * 最大查询数量
         * 管线处理后返回的最大查询数量
         */
        @Min(value = 1, message = "最大查询数量不能少于1")
        @Max(value = 20, message = "最大查询数量不能超过20")
        @Builder.Default
        private Integer maxQueries = 10;
        
        // ========== 新增阶段配置 ==========
        
        /**
         * 是否启用语义对齐阶段
         * 包括同义词归一化、单位标准化、时间正则化
         */
        @Builder.Default
        private Boolean enableSemanticAlignment = true;
        
        /**
         * 是否启用意图抽取阶段  
         * 基于现有意图识别服务进行结构化槽位抽取
         */
        @Builder.Default
        private Boolean enableIntentExtraction = true;
        
        /**
         * 是否启用可检索化改写阶段
         * 包括语义改写、负向词处理、关键词增强
         */
        @Builder.Default
        private Boolean enableRetrievability = true;
        
        /**
         * 是否启用检索增强策略阶段
         * 包括多路Query、Step-back、HyDE等高级策略
         */
        @Builder.Default
        private Boolean enableExpansionStrategy = false; // 默认关闭，可按需开启
        
        /**
         * 语义对齐配置
         */
        private SemanticAlignmentConfig semanticAlignmentConfig;
        
        /**
         * 意图抽取配置
         */
        private IntentExtractionConfig intentExtractionConfig;
        
        /**
         * 可检索化配置
         */
        private RetrievabilityConfig retrievabilityConfig;
        
        /**
         * 扩展策略配置
         */
        private ExpansionStrategyConfig expansionStrategyConfig;
        
        // Getter方法
        public boolean isEnableSemanticAlignment() {
            return enableSemanticAlignment != null && enableSemanticAlignment;
        }
        
        public boolean isEnableIntentExtraction() {
            return enableIntentExtraction != null && enableIntentExtraction;
        }
        
        public boolean isEnableRetrievability() {
            return enableRetrievability != null && enableRetrievability;
        }
        
        public boolean isEnableExpansionStrategy() {
            return enableExpansionStrategy != null && enableExpansionStrategy;
        }
        
        /**
         * 是否保留原始查询
         * 确保原始查询始终包含在结果中
         */
        @Builder.Default
        private Boolean keepOriginal = true;
        
        /**
         * 去重阈值
         * 用于语义去重的相似度阈值，0.0-1.0之间
         */
        @DecimalMin(value = "0.0", message = "去重阈值不能小于0.0")
        @DecimalMax(value = "1.0", message = "去重阈值不能大于1.0")
        @Builder.Default
        private Double dedupThreshold = 0.85;
        
        /**
         * 最大延迟时间（毫秒）
         * 管线处理的最大超时时间
         */
        @Min(value = 1000, message = "最大延迟时间不能少于1000毫秒")
        @Max(value = 60000, message = "最大延迟时间不能超过60000毫秒")
        @Builder.Default
        private Long maxLatencyMs = 30000L;
        
        /**
         * 最大token预算
         * 限制管线处理过程中的token消耗
         */
        @Min(value = 100, message = "最大token预算不能少于100")
        @Max(value = 10000, message = "最大token预算不能超过10000")
        @Builder.Default
        private Integer maxTokens = 2000;
        
        /**
         * 降级策略
         * 当阶段失败时的处理策略
         */
        @Builder.Default
        private String fallbackPolicy = "SKIP_STAGE";
        
        /**
         * 标准化配置
         */
        private NormalizationConfig normalizationConfig;
        
        /**
         * 扩展配置
         */
        private ExpandingConfig expandingConfig;

        /**
         * 是否启用意图识别
         */
        public boolean isIntentRecognitionEnabled() {
            return intentRecognitionEnabled != null && intentRecognitionEnabled;
        }

        /**
         * 获取默认渠道
         */
        public String getDefaultChannel() {
            return defaultChannel != null ? defaultChannel : "web";
        }

        /**
         * 获取默认租户
         */
        public String getDefaultTenant() {
            return defaultTenant != null ? defaultTenant : "default";
        }
        
        /**
         * 是否启用管线化处理
         */
        public boolean isEnablePipeline() {
            return enablePipeline != null && enablePipeline;
        }
        
        /**
         * 是否启用标准化
         */
        public boolean isEnableNormalization() {
            return enableNormalization != null && enableNormalization;
        }
        
        /**
         * 是否启用扩展
         */
        public boolean isEnableExpanding() {
            return enableExpanding != null && enableExpanding;
        }
        
        /**
         * 是否保留原始查询
         */
        public boolean isKeepOriginal() {
            return keepOriginal != null && keepOriginal;
        }
        
        /**
         * 获取标准化配置或默认值
         */
        public NormalizationConfig getNormalizationConfigOrDefault() {
            return normalizationConfig != null ? normalizationConfig : NormalizationConfig.builder().build();
        }
        
        /**
         * 获取扩展配置或默认值
         */
        public ExpandingConfig getExpandingConfigOrDefault() {
            return expandingConfig != null ? expandingConfig : ExpandingConfig.builder().build();
        }
        
        /**
         * 获取语义对齐配置或默认值
         */
        public SemanticAlignmentConfig getSemanticAlignmentConfigOrDefault() {
            return semanticAlignmentConfig != null ? semanticAlignmentConfig : SemanticAlignmentConfig.builder().build();
        }
        
        /**
         * 获取意图抽取配置或默认值
         */
        public IntentExtractionConfig getIntentExtractionConfigOrDefault() {
            return intentExtractionConfig != null ? intentExtractionConfig : IntentExtractionConfig.builder().build();
        }
        
        /**
         * 获取可检索化配置或默认值
         */
        public RetrievabilityConfig getRetrievabilityConfigOrDefault() {
            return retrievabilityConfig != null ? retrievabilityConfig : RetrievabilityConfig.builder().build();
        }
        
        /**
         * 获取扩展策略配置或默认值
         */
        public ExpansionStrategyConfig getExpansionStrategyConfigOrDefault() {
            return expansionStrategyConfig != null ? expansionStrategyConfig : ExpansionStrategyConfig.builder().build();
        }

        // ========== 新增：拼音改写 / 前缀补全 / 近义词召回 ==========

        /**
         * 是否启用拼音改写
         */
        @Builder.Default
        private Boolean enablePhoneticCorrection = false;

        /**
         * 是否启用前缀补全
         */
        @Builder.Default
        private Boolean enablePrefixCompletion = false;

        /**
         * 是否启用近义词召回
         */
        @Builder.Default
        private Boolean enableSynonymRecall = false;

        /**
         * 拼音改写配置
         */
        private PhoneticConfig phoneticConfig;

        /**
         * 前缀补全配置
         */
        private PrefixConfig prefixConfig;

        /**
         * 近义词召回配置
         */
        private SynonymRecallConfig synonymRecallConfig;

        public boolean isEnablePhoneticCorrection() {
            return enablePhoneticCorrection != null && enablePhoneticCorrection;
        }

        public boolean isEnablePrefixCompletion() {
            return enablePrefixCompletion != null && enablePrefixCompletion;
        }

        public boolean isEnableSynonymRecall() {
            return enableSynonymRecall != null && enableSynonymRecall;
        }

        public PhoneticConfig getPhoneticConfigOrDefault() {
            return phoneticConfig != null ? phoneticConfig : PhoneticConfig.builder().build();
        }

        public PrefixConfig getPrefixConfigOrDefault() {
            return prefixConfig != null ? prefixConfig : PrefixConfig.builder().build();
        }

        public SynonymRecallConfig getSynonymRecallConfigOrDefault() {
            return synonymRecallConfig != null ? synonymRecallConfig : SynonymRecallConfig.builder().build();
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PhoneticConfig {
            @Builder.Default
            private Double minConfidence = 0.6;

            @Builder.Default
            private Integer maxCandidates = 3;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PrefixConfig {
            @Builder.Default
            private Integer minPrefixLength = 2;

            @Builder.Default
            private Integer maxCandidates = 5;

            @Builder.Default
            private Boolean onlyShortQuery = true;

            @Builder.Default
            private Integer shortQueryMaxLen = 5;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SynonymRecallConfig {
            private Long embeddingModelId;

            @Builder.Default
            private Integer topK = 5;

            @Builder.Default
            private Double simThreshold = 0.7;
        }
        
        /**
         * 标准化配置
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NormalizationConfig {
            /**
             * 是否去除停用词
             */
            @Builder.Default
            private Boolean removeStopwords = false;
            
            /**
             * 最大查询长度
             */
            @Min(value = 10, message = "最大查询长度不能少于10")
            @Max(value = 1000, message = "最大查询长度不能超过1000")
            @Builder.Default
            private Integer maxQueryLength = 512;
            
            /**
             * 是否标准化大小写
             */
            @Builder.Default
            private Boolean normalizeCase = true;
            
            /**
             * 是否清理多余空白
             */
            @Builder.Default
            private Boolean cleanWhitespace = true;
            
            public boolean isRemoveStopwords() {
                return removeStopwords != null && removeStopwords;
            }
            
            public boolean isNormalizeCase() {
                return normalizeCase != null && normalizeCase;
            }
            
            public boolean isCleanWhitespace() {
                return cleanWhitespace != null && cleanWhitespace;
            }
        }
        
        /**
         * 扩展配置
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ExpandingConfig {
            /**
             * 扩展数量（覆盖父级n配置）
             */
            @Min(value = 1, message = "扩展数量不能少于1")
            @Max(value = 10, message = "扩展数量不能超过10")
            private Integer n;
            
            /**
             * 提示模板（覆盖父级promptTemplate配置）
             */
            private String promptTemplate;
            
            /**
             * 温度参数
             * 控制生成结果的随机性，0.0-2.0之间
             */
            @DecimalMin(value = "0.0", message = "温度参数不能小于0.0")
            @DecimalMax(value = "2.0", message = "温度参数不能大于2.0")
            @Builder.Default
            private Double temperature = 0.7;
            
            /**
             * 获取实际的扩展数量，如果未设置则使用父级配置
             */
            public int getActualN(int parentN) {
                return n != null ? n : parentN;
            }
            
            /**
             * 获取实际的提示模板，如果未设置则使用父级配置
             */
            public String getActualPromptTemplate(String parentPromptTemplate) {
                return promptTemplate != null && !promptTemplate.trim().isEmpty() 
                    ? promptTemplate : parentPromptTemplate;
            }
        }
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


}
