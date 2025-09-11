package com.leyue.smartcs.domain.ltm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 情景记忆领域实体
 * 存储具体时间、地点的交互事件记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodicMemory {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID，可为空表示跨会话记忆
     */
    private Long sessionId;

    /**
     * 情节ID，唯一标识符
     */
    private String episodeId;

    /**
     * 情节内容，包含对话片段和上下文
     */
    private String content;

    /**
     * 向量嵌入，用于语义检索
     */
    private byte[] embeddingVector;

    /**
     * 上下文元数据：时间、地点、情绪、参与者等
     */
    private Map<String, Object> contextMetadata;

    /**
     * 发生时间戳
     */
    private Long timestamp;

    /**
     * 重要性评分 0.000-1.000
     */
    private Double importanceScore;

    /**
     * 访问次数，用于计算记忆强度
     */
    private Integer accessCount;

    /**
     * 最后访问时间
     */
    private Long lastAccessedAt;

    /**
     * 巩固状态 0=新记忆 1=已巩固 2=已归档
     */
    private Integer consolidationStatus;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;

    /**
     * 是否为重要记忆
     */
    public boolean isImportant() {
        return importanceScore != null && importanceScore >= 0.7;
    }

    /**
     * 是否为高频记忆
     */
    public boolean isHighFrequency() {
        return accessCount != null && accessCount >= 10;
    }

    /**
     * 是否需要巩固
     */
    public boolean needsConsolidation() {
        return consolidationStatus == 0 && isImportant();
    }

    /**
     * 增加访问次数
     */
    public void increaseAccessCount() {
        if (this.accessCount == null) {
            this.accessCount = 0;
        }
        this.accessCount++;
        this.lastAccessedAt = System.currentTimeMillis();
    }

    /**
     * 更新重要性评分
     */
    public void updateImportanceScore(Double newScore) {
        this.importanceScore = newScore;
        if (newScore >= 0.7 && this.consolidationStatus == 0) {
            // 标记需要巩固
            this.consolidationStatus = 0;
        }
    }

    /**
     * 标记为已巩固
     */
    public void markAsConsolidated() {
        this.consolidationStatus = 1;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 标记为已归档
     */
    public void markAsArchived() {
        this.consolidationStatus = 2;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 添加上下文元数据
     */
    public void addContextMetadata(String key, Object value) {
        if (this.contextMetadata != null) {
            this.contextMetadata.put(key, value);
        }
    }
}