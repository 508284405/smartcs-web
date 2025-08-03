package com.leyue.smartcs.rag.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息查询条件
 * 用于构建复杂的消息查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageQuery {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 消息角色列表
     */
    private List<String> roles;

    /**
     * 消息类型列表
     */
    private List<String> types;

    /**
     * 模型ID列表
     */
    private List<Long> modelIds;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 内容关键词
     */
    private String keyword;

    /**
     * 最小Token数
     */
    private Integer minTokens;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 是否包含已删除消息
     */
    private Boolean includeDeleted;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderDirection;

    /**
     * 分页偏移量
     */
    private Integer offset;

    /**
     * 分页限制
     */
    private Integer limit;

    /**
     * 消息状态列表
     */
    private List<String> statuses;

    /**
     * 父消息ID
     */
    private String parentMessageId;

    /**
     * 是否包含错误消息
     */
    private Boolean includeErrors;

    /**
     * 最小响应时间（毫秒）
     */
    private Long minResponseTime;

    /**
     * 最大响应时间（毫秒）
     */
    private Long maxResponseTime;
}