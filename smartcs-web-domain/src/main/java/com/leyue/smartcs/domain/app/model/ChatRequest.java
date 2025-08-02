package com.leyue.smartcs.domain.app.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 聊天请求领域模型
 * 纯业务对象，不依赖任何技术框架
 */
@Data
@Builder
public class ChatRequest {
    
    /**
     * 会话标识符
     */
    private String sessionId;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    
    /**
     * 用户消息
     */
    private String userMessage;
    
    /**
     * 模板变量
     */
    private Map<String, Object> variables;
    
    /**
     * 知识库ID（可选，用于RAG）
     */
    private Long knowledgeBaseId;
    
    /**
     * 是否包含历史记忆
     */
    private Boolean includeMemory;
    
    /**
     * 是否启用流式响应
     */
    private Boolean streamEnabled;
    
    /**
     * 请求上下文信息
     */
    private String context;
    
    /**
     * 创建简单聊天请求
     */
    public static ChatRequest simple(String sessionId, String systemPrompt, String userMessage) {
        return ChatRequest.builder()
                .sessionId(sessionId)
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .includeMemory(true)
                .streamEnabled(false)
                .build();
    }
    
    /**
     * 创建流式聊天请求
     */
    public static ChatRequest streaming(String sessionId, String systemPrompt, String userMessage) {
        return ChatRequest.builder()
                .sessionId(sessionId)
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .includeMemory(true)
                .streamEnabled(true)
                .build();
    }
    
    /**
     * 创建RAG聊天请求
     */
    public static ChatRequest withRag(String sessionId, String systemPrompt, String userMessage, Long knowledgeBaseId) {
        return ChatRequest.builder()
                .sessionId(sessionId)
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .knowledgeBaseId(knowledgeBaseId)
                .includeMemory(true)
                .streamEnabled(false)
                .build();
    }
    
    /**
     * 创建无记忆聊天请求
     */
    public static ChatRequest withoutMemory(String systemPrompt, String userMessage) {
        return ChatRequest.builder()
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .includeMemory(false)
                .streamEnabled(false)
                .build();
    }
    
    /**
     * 是否启用RAG
     */
    public boolean isRagEnabled() {
        return knowledgeBaseId != null;
    }
    
    /**
     * 是否有记忆
     */
    public boolean hasMemory() {
        return Boolean.TRUE.equals(includeMemory);
    }
    
    /**
     * 是否启用流式
     */
    public boolean isStreaming() {
        return Boolean.TRUE.equals(streamEnabled);
    }
}