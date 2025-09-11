package com.leyue.smartcs.domain.ltm.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 记忆巩固事件
 * 当记忆完成巩固过程时触发的领域事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryConsolidatedEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 巩固类型：episodic_to_semantic/episodic_to_procedural/pattern_strengthening
     */
    private String consolidationType;

    /**
     * 源记忆类型
     */
    private String sourceMemoryType;

    /**
     * 源记忆ID列表
     */
    private List<Long> sourceMemoryIds;

    /**
     * 目标记忆类型
     */
    private String targetMemoryType;

    /**
     * 目标记忆ID
     */
    private Long targetMemoryId;

    /**
     * 巩固摘要描述
     */
    private String consolidationSummary;

    /**
     * 置信度或成功率
     */
    private Double confidence;

    /**
     * 巩固过程中的上下文信息
     */
    private Map<String, Object> context;

    /**
     * 事件发生时间
     */
    private Long timestamp;

    /**
     * 巩固算法版本
     */
    private String algorithmVersion;

    /**
     * 事件元数据
     */
    private Map<String, Object> metadata;
}