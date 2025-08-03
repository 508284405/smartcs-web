package com.leyue.smartcs.rag.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话持久化实体
 * 用于数据库存储的会话表示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {

    /**
     * 会话ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话状态
     */
    private String status;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 知识库ID列表（JSON格式）
     */
    private String knowledgeBaseIds;

    /**
     * 会话配置（JSON格式）
     */
    private String configuration;

    /**
     * 总消息数
     */
    private Integer messageCount;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 会话类型
     */
    private String sessionType;

    /**
     * 会话来源
     */
    private String source;

    /**
     * 元数据（JSON格式）
     */
    private String metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 结束时间
     */
    private LocalDateTime endedAt;

    /**
     * 是否已删除
     */
    private Boolean deleted;

    /**
     * 会话持续时间（秒）
     */
    private Long duration;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 会话评分
     */
    private Double rating;

    /**
     * 会话反馈
     */
    private String feedback;
}