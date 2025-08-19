package com.leyue.smartcs.dto.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 结构化聊天响应
 * 用于LangChain4j结构化输出
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredChatResponse {

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
     * 是否需要进一步澄清
     */
    private Boolean needsClarification;

    /**
     * 相关知识来源
     */
    private List<KnowledgeSource> sources;

    /**
     * 建议的后续问题
     */
    private List<String> suggestedQuestions;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 响应类型枚举
     */
    public enum ResponseType {
        ANSWER,          // 直接回答
        CLARIFICATION,   // 需要澄清
        SUGGESTION,      // 建议
        ERROR,           // 错误
        KNOWLEDGE_BASED, // 基于知识库
        GENERAL          // 一般对话
    }

    /**
     * 知识来源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSource {
        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源标题
         */
        private String title;

        /**
         * 相关性分数
         */
        private Double relevanceScore;

        /**
         * 摘要
         */
        private String summary;
    }
}