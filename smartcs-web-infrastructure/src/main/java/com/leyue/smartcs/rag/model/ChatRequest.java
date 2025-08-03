package com.leyue.smartcs.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG聊天请求
 * 封装所有聊天相关的输入参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户消息
     */
    private String message;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 模型ID（用于动态获取模型）
     */
    private Long modelId;

    /**
     * 知识库ID列表（支持多知识库，可选）
     */
    private List<Long> knowledgeBaseIds;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 是否启用工具
     */
    private Boolean enableTools;

    /**
     * 是否启用MCP
     */
    private Boolean enableMcp;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 扩展参数
     */
    private Map<String, Object> metadata;

    /**
     * 是否流式响应
     */
    private Boolean streaming;
}