package com.leyue.smartcs.dto.chat;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息搜索查询
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageSearchQry extends PageQuery {
    
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
     * 对应 MessageType 枚举值
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
    private String sortBy = "time";
    
    /**
     * 是否只搜索自己发送的消息
     */
    private Boolean onlyMyMessages = false;
    
    /**
     * 是否包含系统消息
     */
    private Boolean includeSystemMessages = true;
}