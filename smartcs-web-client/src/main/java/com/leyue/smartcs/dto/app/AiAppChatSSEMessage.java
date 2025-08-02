package com.leyue.smartcs.dto.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI应用聊天SSE消息封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAppChatSSEMessage {
    
    /**
     * 消息类型
     */
    private MessageType type;
    
    /**
     * 消息数据
     */
    private Object data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * SSE消息类型枚举
     */
    public enum MessageType {
        START("start"),           // 开始处理
        PROGRESS("progress"),     // 处理进度
        DATA("data"),            // 数据消息（流式响应）
        COMPLETE("complete"),    // 完成消息
        ERROR("error"),          // 错误消息
        TIMEOUT("timeout");      // 超时消息
        
        private final String value;
        
        MessageType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 创建开始消息
     */
    public static AiAppChatSSEMessage start(String sessionId) {
        return AiAppChatSSEMessage.builder()
                .type(MessageType.START)
                .data(sessionId)
                .timestamp(System.currentTimeMillis())
                .id("start_" + sessionId)
                .build();
    }
    
    /**
     * 创建进度消息
     */
    public static AiAppChatSSEMessage progress(String sessionId, String message) {
        return AiAppChatSSEMessage.builder()
                .type(MessageType.PROGRESS)
                .data(message)
                .timestamp(System.currentTimeMillis())
                .id("progress_" + sessionId + "_" + System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建数据消息
     */
    public static AiAppChatSSEMessage data(String sessionId, AiAppChatResponse response) {
        return AiAppChatSSEMessage.builder()
                .type(MessageType.DATA)
                .data(response)
                .timestamp(System.currentTimeMillis())
                .id("data_" + sessionId + "_" + System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建完成消息
     */
    public static AiAppChatSSEMessage complete(String sessionId, AiAppChatResponse response) {
        return AiAppChatSSEMessage.builder()
                .type(MessageType.COMPLETE)
                .data(response)
                .timestamp(System.currentTimeMillis())
                .id("complete_" + sessionId)
                .build();
    }
    
    /**
     * 创建错误消息
     */
    public static AiAppChatSSEMessage error(String sessionId, String errorMessage) {
        return AiAppChatSSEMessage.builder()
                .type(MessageType.ERROR)
                .data(errorMessage)
                .timestamp(System.currentTimeMillis())
                .id("error_" + sessionId)
                .build();
    }
    
    /**
     * 创建超时消息
     */
    public static AiAppChatSSEMessage timeout(String sessionId) {
        return AiAppChatSSEMessage.builder()
                .type(MessageType.TIMEOUT)
                .data("连接超时")
                .timestamp(System.currentTimeMillis())
                .id("timeout_" + sessionId)
                .build();
    }
}