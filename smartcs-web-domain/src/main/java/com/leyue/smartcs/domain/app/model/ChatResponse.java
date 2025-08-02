package com.leyue.smartcs.domain.app.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 聊天响应领域模型
 * 纯业务对象，不依赖任何技术框架
 */
@Data
@Builder
public class ChatResponse {
    
    /**
     * 会话标识符
     */
    private String sessionId;
    
    /**
     * 消息标识符
     */
    private String messageId;
    
    /**
     * AI响应内容
     */
    private String content;
    
    /**
     * 响应是否完成
     */
    private Boolean finished;
    
    /**
     * 响应生成时间戳
     */
    private Long timestamp;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processTime;
    
    /**
     * 置信度（0-1）
     */
    private Double confidence;
    
    /**
     * 知识来源信息
     */
    private List<String> knowledgeSources;
    
    /**
     * 建议的后续问题
     */
    private List<String> suggestedQuestions;
    
    /**
     * 响应类型
     */
    private ResponseType responseType;
    
    /**
     * 是否需要澄清
     */
    private Boolean needsClarification;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> metadata;
    
    /**
     * 响应类型枚举
     */
    public enum ResponseType {
        ANSWER("回答"),
        CLARIFICATION("澄清"),
        SUGGESTION("建议"),
        ERROR("错误"),
        KNOWLEDGE_BASED("基于知识库"),
        GENERAL("通用");
        
        private final String description;
        
        ResponseType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 创建简单响应
     */
    public static ChatResponse simple(String sessionId, String messageId, String content) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .messageId(messageId)
                .content(content)
                .finished(true)
                .timestamp(System.currentTimeMillis())
                .responseType(ResponseType.ANSWER)
                .build();
    }
    
    /**
     * 创建流式响应片段
     */
    public static ChatResponse streamingChunk(String sessionId, String messageId, String content) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .messageId(messageId)
                .content(content)
                .finished(false)
                .timestamp(System.currentTimeMillis())
                .responseType(ResponseType.ANSWER)
                .build();
    }
    
    /**
     * 创建完成的流式响应
     */
    public static ChatResponse streamingComplete(String sessionId, String messageId, String fullContent, Long processTime) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .messageId(messageId)
                .content(fullContent)
                .finished(true)
                .timestamp(System.currentTimeMillis())
                .processTime(processTime)
                .responseType(ResponseType.ANSWER)
                .build();
    }
    
    /**
     * 创建基于知识库的响应
     */
    public static ChatResponse knowledgeBased(String sessionId, String messageId, String content, 
                                            List<String> sources, Double confidence) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .messageId(messageId)
                .content(content)
                .finished(true)
                .timestamp(System.currentTimeMillis())
                .knowledgeSources(sources)
                .confidence(confidence)
                .responseType(ResponseType.KNOWLEDGE_BASED)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static ChatResponse error(String sessionId, String errorMessage) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .content(errorMessage)
                .finished(true)
                .timestamp(System.currentTimeMillis())
                .responseType(ResponseType.ERROR)
                .build();
    }
    
    /**
     * 判断是否完成
     */
    public boolean isComplete() {
        return Boolean.TRUE.equals(finished);
    }
    
    /**
     * 判断是否为错误响应
     */
    public boolean isError() {
        return ResponseType.ERROR.equals(responseType);
    }
    
    /**
     * 判断是否基于知识库
     */
    public boolean isKnowledgeBased() {
        return ResponseType.KNOWLEDGE_BASED.equals(responseType);
    }
}