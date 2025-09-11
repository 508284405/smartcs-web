package com.leyue.smartcs.domain.ltm.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 记忆形成事件
 * 当新记忆形成时触发的领域事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryFormedEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 记忆类型：episodic/semantic/procedural
     */
    private String memoryType;

    /**
     * 记忆ID
     */
    private Long memoryId;

    /**
     * 记忆内容摘要
     */
    private String contentSummary;

    /**
     * 重要性评分
     */
    private Double importance;

    /**
     * 上下文信息
     */
    private Map<String, Object> context;

    /**
     * 事件发生时间
     */
    private Long timestamp;

    /**
     * 来源会话ID（可选）
     */
    private Long sessionId;

    /**
     * 触发器类型：conversation/action/pattern_recognition
     */
    private String triggerType;

    /**
     * 事件元数据
     */
    private Map<String, Object> metadata;
}