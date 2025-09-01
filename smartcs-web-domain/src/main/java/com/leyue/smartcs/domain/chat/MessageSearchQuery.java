package com.leyue.smartcs.domain.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 消息搜索查询领域对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchQuery {
    
    /**
     * 用户ID（搜索发起者）
     */
    private String userId;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 会话ID（可选，不指定则搜索所有会话）
     */
    private Long sessionId;
    
    /**
     * 消息类型筛选（可选）
     */
    private Integer messageType;
    
    /**
     * 搜索开始时间（可选）
     */
    private Long startTime;
    
    /**
     * 搜索结束时间（可选）
     */
    private Long endTime;
    
    /**
     * 排序方式（可选）
     * time: 按时间排序（默认）
     * relevance: 按相关性排序
     */
    @Builder.Default
    private String sortBy = "time";
    
    /**
     * 是否只搜索自己发送的消息
     */
    @Builder.Default
    private Boolean onlyMyMessages = false;
    
    /**
     * 是否包含系统消息
     */
    @Builder.Default
    private Boolean includeSystemMessages = true;
    
    /**
     * 偏移量
     */
    @Builder.Default
    private int offset = 0;
    
    /**
     * 限制数量
     */
    @Builder.Default
    private int limit = 20;
}