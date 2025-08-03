package com.leyue.smartcs.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RAG聊天响应
 * 封装聊天的输出结果和相关元数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 响应内容
     */
    private String content;

    /**
     * 响应类型
     */
    private ResponseType type;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 是否完成
     */
    private Boolean finished;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 知识库来源
     */
    private List<KnowledgeSource> knowledgeSources;

    /**
     * 使用的工具
     */
    private List<ToolUsage> toolUsages;

    /**
     * Token使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;

    /**
     * 响应类型枚举
     */
    public enum ResponseType {
        NORMAL,           // 正常响应
        KNOWLEDGE_BASED,  // 基于知识库的响应
        TOOL_ENHANCED,    // 工具增强的响应
        ERROR,            // 错误响应
        STREAMING         // 流式响应片段
    }

    /**
     * 知识来源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSource {
        private String sourceId;
        private String sourceType;
        private String title;
        private String content;
        private Double relevanceScore;
        private Long knowledgeBaseId;
        private Map<String, Object> metadata;
    }

    /**
     * 工具使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolUsage {
        private String toolName;
        private String toolType;
        private Map<String, Object> parameters;
        private Object result;
        private Long executionTime;
        private Boolean success;
    }

    /**
     * Token使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}