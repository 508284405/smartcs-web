package com.leyue.smartcs.dto.eval.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG事件DTO - 用于评估系统的事件收集
 * 包含RAG管道的完整元数据信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagEvent {
    
    /**
     * 事件唯一标识
     */
    private String eventId;
    
    /**
     * 链路追踪ID
     */
    private String traceId;
    
    /**
     * 时间戳(毫秒)
     */
    private Long ts;
    
    /**
     * 用户ID(匿名化处理)
     */
    private String userId;
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * AI答案
     */
    private String answer;
    
    /**
     * 标准答案(用于评估)
     */
    private String groundTruth;
    
    /**
     * 检索上下文列表
     */
    private List<RetrievedContext> retrievedContexts;
    
    /**
     * 引用列表
     */
    private List<Citation> citations;
    
    /**
     * LLM配置信息
     */
    private LlmConfig llm;
    
    /**
     * 检索器配置信息
     */
    private RetrieverConfig retriever;
    
    /**
     * 总延迟(毫秒)
     */
    private Long latencyMs;
    
    /**
     * 应用信息
     */
    private AppInfo app;
    
    /**
     * 检索上下文
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedContext {
        /**
         * 文档ID
         */
        private String docId;
        
        /**
         * 文本内容
         */
        private String text;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 排名
         */
        private Integer rank;
        
        /**
         * 来源
         */
        private String source;
        
        /**
         * 文档块ID
         */
        private String chunkId;
    }
    
    /**
     * 引用信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Citation {
        /**
         * 引用文本
         */
        private String text;
        
        /**
         * 来源
         */
        private String source;
        
        /**
         * 文档ID
         */
        private String docId;
    }
    
    /**
     * LLM配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LlmConfig {
        /**
         * 模型名称
         */
        private String model;
        
        /**
         * 温度参数
         */
        private Double temperature;
        
        /**
         * 提供商
         */
        private String provider;
    }
    
    /**
     * 检索器配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrieverConfig {
        /**
         * Top-K数量
         */
        private Integer k;
        
        /**
         * 嵌入模型
         */
        private String embeddingModel;
        
        /**
         * 索引版本
         */
        private String index;
        
        /**
         * 重排序器
         */
        private String reranker;
    }
    
    /**
     * 应用信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppInfo {
        /**
         * 服务名称
         */
        private String service;
        
        /**
         * 版本号
         */
        private String version;
    }
}