package com.leyue.smartcs.dto.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSEMessage {
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 消息事件类型
     */
    private String event;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 消息数据
     */
    private Object data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 创建开始消息
     */
    public static SSEMessage start(Long sessionId) {
        return SSEMessage.builder()
                .id("start_" + sessionId)
                .event("start")
                .sessionId(sessionId)
                .data("开始处理")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建进度消息
     */
    public static SSEMessage progress(Long sessionId, String message) {
        return SSEMessage.builder()
                .id("progress_" + sessionId + "_" + System.currentTimeMillis())
                .event("progress")
                .sessionId(sessionId)
                .data(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建数据消息
     */
    public static SSEMessage data(Long sessionId, Object data) {
        return SSEMessage.builder()
                .id("data_" + sessionId + "_" + System.currentTimeMillis())
                .event("data")
                .sessionId(sessionId)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建完成消息
     */
    public static SSEMessage complete(Long sessionId, Object finalData) {
        return SSEMessage.builder()
                .id("complete_" + sessionId)
                .event("complete")
                .sessionId(sessionId)
                .data(finalData)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建错误消息
     */
    public static SSEMessage error(Long sessionId, String error) {
        return SSEMessage.builder()
                .id("error_" + sessionId)
                .event("error")
                .sessionId(sessionId)
                .data(error)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}