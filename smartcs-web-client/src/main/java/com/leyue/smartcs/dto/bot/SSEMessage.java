package com.leyue.smartcs.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * SSE消息DTO
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SSEMessage {
    /** 消息类型 */
    private String type;
    /** 消息数据 */
    private Object data;
    /** 时间戳 */
    private Long timestamp;
    /** 消息ID */
    private String id;
    /**
     * 创建开始消息
     */
    public static SSEMessage start(Long sessionId) {
        return new SSEMessage()
                .setType("start")
                .setData(sessionId)
                .setTimestamp(System.currentTimeMillis())
                .setId("start_" + sessionId);
    }
    
    /**
     * 创建进度消息
     */
    public static SSEMessage progress(Long sessionId, String message) {
        return new SSEMessage()
                .setType("progress")
                .setData(message)
                .setTimestamp(System.currentTimeMillis())
                .setId("progress_" + sessionId + "_" + System.currentTimeMillis());
    }
    
    /**
     * 创建数据消息
     */
    public static SSEMessage data(Long sessionId, Object data) {
        return new SSEMessage()
                .setType("data")
                .setData(data)
                .setTimestamp(System.currentTimeMillis())
                .setId("data_" + sessionId + "_" + System.currentTimeMillis());
    }
    
    /**
     * 创建完成消息
     */
    public static SSEMessage complete(Long sessionId, Object finalData) {
        return new SSEMessage()
                .setType("complete")
                .setData(finalData)
                .setTimestamp(System.currentTimeMillis())
                .setId("complete_" + sessionId);
    }
    
    /**
     * 创建错误消息
     */
    public static SSEMessage error(Long sessionId, String error) {
        return new SSEMessage()
                .setType("error")
                .setData(error)
                .setTimestamp(System.currentTimeMillis())
                .setId("error_" + sessionId);
    }
} 