package com.leyue.smartcs.dto.bot;

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
     * 消息类型
     */
    private String type;
    
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
     * 创建开始消息
     */
    public static SSEMessage start(Long sessionId) {
        return SSEMessage.builder()
                .type("start")
                .data(sessionId)
                .timestamp(System.currentTimeMillis())
                .id("start_" + sessionId)
                .build();
    }
    
    /**
     * 创建进度消息
     */
    public static SSEMessage progress(Long sessionId, String message) {
        return SSEMessage.builder()
                .type("progress")
                .data(message)
                .timestamp(System.currentTimeMillis())
                .id("progress_" + sessionId + "_" + System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建数据消息
     */
    public static SSEMessage data(Long sessionId, Object data) {
        return SSEMessage.builder()
                .type("data")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .id("data_" + sessionId + "_" + System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建完成消息
     */
    public static SSEMessage complete(Long sessionId, Object finalData) {
        return SSEMessage.builder()
                .type("complete")
                .data(finalData)
                .timestamp(System.currentTimeMillis())
                .id("complete_" + sessionId)
                .build();
    }
    
    /**
     * 创建错误消息
     */
    public static SSEMessage error(Long sessionId, String error) {
        return SSEMessage.builder()
                .type("error")
                .data(error)
                .timestamp(System.currentTimeMillis())
                .id("error_" + sessionId)
                .build();
    }
} 