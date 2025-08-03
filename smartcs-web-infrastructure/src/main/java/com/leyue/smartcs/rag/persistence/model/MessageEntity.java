package com.leyue.smartcs.rag.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消息持久化实体
 * 用于数据库存储的消息表示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    /**
     * 消息ID
     */
    private String id;

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
     * 消息角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String type;

    /**
     * Token数量
     */
    private Integer tokens;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 知识库来源
     */
    private String knowledgeSources;

    /**
     * 工具使用情况
     */
    private String toolUsages;

    /**
     * 元数据（JSON格式）
     */
    private String metadata;

    /**
     * 消息来源
     */
    private String source;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否已删除
     */
    private Boolean deleted;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 父消息ID（用于消息关联）
     */
    private String parentMessageId;

    /**
     * 消息序号
     */
    private Integer sequenceNumber;
}