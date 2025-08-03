package com.leyue.smartcs.rag.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话状态
 * 描述RAG会话的当前状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatus {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 会话状态
     */
    private Status status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 当前消息数量
     */
    private Integer messageCount;

    /**
     * 是否在处理中
     */
    private Boolean processing;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 会话状态枚举
     */
    public enum Status {
        INITIALIZING,  // 初始化中
        ACTIVE,        // 活跃
        IDLE,          // 空闲
        PROCESSING,    // 处理中
        ERROR,         // 错误
        CLOSED         // 已关闭
    }
}