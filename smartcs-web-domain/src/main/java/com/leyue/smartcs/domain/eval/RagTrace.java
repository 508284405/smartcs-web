package com.leyue.smartcs.domain.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG追踪数据模型
 * 用于记录RAG流程中每个阶段的详细信息，支持评估系统的观察和分析
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagTrace {
    
    /**
     * 追踪ID，唯一标识一次RAG流程
     */
    private String traceId;
    
    /**
     * 关联的会话ID
     */
    private String sessionId;
    
    /**
     * 原始查询文本
     */
    private String originalQuery;
    
    /**
     * 查询重写后的文本（如果有查询重写步骤）
     */
    private String rewrittenQuery;
    
    /**
     * 路由和检索器选择信息
     */
    private RoutingInfo routingInfo;
    
    /**
     * 检索阶段的详细信息
     */
    private RetrievalStage retrievalStage;
    
    /**
     * 重排序阶段的详细信息
     */
    private RerankStage rerankStage;
    
    /**
     * 上下文注入阶段的详细信息
     */
    private ContextInjectionStage contextInjectionStage;
    
    /**
     * 生成阶段的详细信息
     */
    private GenerationStage generationStage;
    
    /**
     * 各阶段的延迟信息（毫秒）
     */
    private LatencyInfo latencyInfo;
    
    /**
     * Token使用统计
     */
    private TokenUsage tokenUsage;
    
    /**
     * 错误信息（如果有）
     */
    private ErrorInfo errorInfo;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 追踪开始时间（毫秒时间戳）
     */
    private Long startTime;
    
    /**
     * 追踪结束时间（毫秒时间戳）
     */
    private Long endTime;
    
    /**
     * 路由信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoutingInfo {
        /**
         * 选择的检索策略
         */
        private String retrievalStrategy;
        
        /**
         * 使用的向量存储类型
         */
        private String vectorStoreType;
        
        /**
         * 使用的嵌入模型
         */
        private String embeddingModel;
        
        /**
         * 路由决策依据
         */
        private String routingReason;
    }
    
    /**
     * 检索阶段信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RetrievalStage {
        /**
         * 查询向量化耗时（毫秒）
         */
        private Integer queryEmbeddingTime;
        
        /**
         * 检索到的候选文档列表
         */
        private List<RetrievalCandidate> candidates;
        
        /**
         * 检索参数
         */
        private RetrievalParams params;
        
        /**
         * 向量相似度分布统计
         */
        private ScoreDistribution scoreDistribution;
    }
    
    /**
     * 重排序阶段信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RerankStage {
        /**
         * 重排序后的候选文档列表
         */
        private List<RetrievalCandidate> rerankCandidates;
        
        /**
         * 使用的重排序模型
         */
        private String rerankModel;
        
        /**
         * 重排序参数
         */
        private RerankParams params;
        
        /**
         * 重排序改进程度
         */
        private Double improvementScore;
    }
    
    /**
     * 上下文注入阶段信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContextInjectionStage {
        /**
         * 最终选择的上下文片段
         */
        private List<ContextFragment> selectedContexts;
        
        /**
         * 注入策略
         */
        private String injectionStrategy;
        
        /**
         * 上下文压缩比例
         */
        private Double compressionRatio;
        
        /**
         * 最终提示词摘要
         */
        private String finalPromptSummary;
    }
    
    /**
     * 生成阶段信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerationStage {
        /**
         * 生成的答案文本
         */
        private String generatedAnswer;
        
        /**
         * 使用的生成模型
         */
        private String generationModel;
        
        /**
         * 模型参数
         */
        private ModelParams modelParams;
        
        /**
         * 引用信息
         */
        private List<Citation> citations;
        
        /**
         * 是否启用流式生成
         */
        private Boolean streamingEnabled;
    }
    
    /**
     * 延迟信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LatencyInfo {
        /**
         * 检索延迟（毫秒）
         */
        private Integer retrievalLatency;
        
        /**
         * 重排序延迟（毫秒）
         */
        private Integer rerankLatency;
        
        /**
         * 上下文注入延迟（毫秒）
         */
        private Integer contextInjectionLatency;
        
        /**
         * 生成延迟（毫秒）
         */
        private Integer generationLatency;
        
        /**
         * 总延迟（毫秒）
         */
        private Integer totalLatency;
    }
    
    /**
     * Token使用统计
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenUsage {
        /**
         * 输入Token数量
         */
        private Integer inputTokens;
        
        /**
         * 输出Token数量
         */
        private Integer outputTokens;
        
        /**
         * 总Token数量
         */
        private Integer totalTokens;
        
        /**
         * 成本估算（美元）
         */
        private Double costEstimate;
    }
    
    /**
     * 错误信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorInfo {
        /**
         * 错误阶段
         */
        private String errorStage;
        
        /**
         * 错误类型
         */
        private String errorType;
        
        /**
         * 错误消息
         */
        private String errorMessage;
        
        /**
         * 错误堆栈
         */
        private String stackTrace;
        
        /**
         * 错误时间戳
         */
        private Long errorTimestamp;
    }
    
    /**
     * 检索候选文档
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RetrievalCandidate {
        /**
         * 文档ID
         */
        private String documentId;
        
        /**
         * 文档内容片段
         */
        private String content;
        
        /**
         * 向量相似度分数
         */
        private Double vectorScore;
        
        /**
         * 重排序分数
         */
        private Double rerankScore;
        
        /**
         * 文档元数据
         */
        private Map<String, Object> metadata;
        
        /**
         * 排名位置
         */
        private Integer rank;
    }
    
    /**
     * 检索参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RetrievalParams {
        /**
         * TopK参数
         */
        private Integer topK;
        
        /**
         * 相似度阈值
         */
        private Double similarityThreshold;
        
        /**
         * 搜索类型
         */
        private String searchType;
        
        /**
         * 过滤条件
         */
        private Map<String, Object> filters;
    }
    
    /**
     * 重排序参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RerankParams {
        /**
         * 重排序TopK
         */
        private Integer topK;
        
        /**
         * 重排序阈值
         */
        private Double threshold;
        
        /**
         * 重排序模型参数
         */
        private Map<String, Object> modelParams;
    }
    
    /**
     * 上下文片段
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContextFragment {
        /**
         * 片段ID
         */
        private String fragmentId;
        
        /**
         * 片段内容
         */
        private String content;
        
        /**
         * 来源文档ID
         */
        private String sourceDocumentId;
        
        /**
         * 在最终上下文中的位置
         */
        private Integer position;
        
        /**
         * 重要性分数
         */
        private Double importance;
    }
    
    /**
     * 模型参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModelParams {
        /**
         * 温度参数
         */
        private Double temperature;
        
        /**
         * TopP参数
         */
        private Double topP;
        
        /**
         * 最大Token数
         */
        private Integer maxTokens;
        
        /**
         * 其他参数
         */
        private Map<String, Object> otherParams;
    }
    
    /**
     * 引用信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Citation {
        /**
         * 引用的文档ID
         */
        private String documentId;
        
        /**
         * 引用的内容片段
         */
        private String citedContent;
        
        /**
         * 在答案中的位置
         */
        private Integer position;
        
        /**
         * 置信度
         */
        private Double confidence;
    }
    
    /**
     * 分数分布统计
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScoreDistribution {
        /**
         * 最高分数
         */
        private Double maxScore;
        
        /**
         * 最低分数
         */
        private Double minScore;
        
        /**
         * 平均分数
         */
        private Double avgScore;
        
        /**
         * 分数标准差
         */
        private Double stdDev;
    }
}