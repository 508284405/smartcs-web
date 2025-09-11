package com.leyue.smartcs.client.ltm.dto.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 情景记忆DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodicMemoryDTO {

    /**
     * 记忆ID
     */
    private Long id;

    /**
     * 情节ID
     */
    private String episodeId;

    /**
     * 记忆内容（摘要）
     */
    private String contentSummary;

    /**
     * 完整内容（可选，需要时加载）
     */
    private String fullContent;

    /**
     * 重要性评分
     */
    private Double importanceScore;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 创建时间
     */
    private Long timestamp;

    /**
     * 最后访问时间
     */
    private Long lastAccessedAt;

    /**
     * 巩固状态
     */
    private ConsolidationStatus consolidationStatus;

    /**
     * 上下文元数据
     */
    private Map<String, Object> contextMetadata;

    /**
     * 相关会话信息
     */
    private SessionContextDTO sessionContext;

    /**
     * 衍生的语义记忆
     */
    private java.util.List<RelatedMemoryDTO> derivedSemanticMemories;

    /**
     * 标签
     */
    private java.util.List<String> tags;

    /**
     * 是否被标记为重要
     */
    private Boolean isMarkedImportant;

    /**
     * 用户备注
     */
    private String userNote;

    /**
     * 巩固状态枚举
     */
    public enum ConsolidationStatus {
        NEW("新记忆"),
        CONSOLIDATED("已巩固"),
        ARCHIVED("已归档");

        private final String description;

        ConsolidationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionContextDTO {
        private Long sessionId;
        private String sessionName;
        private Integer messageCount;
        private Long sessionStartTime;
        private String conversationTopic;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedMemoryDTO {
        private Long memoryId;
        private String memoryType;
        private String title;
        private Double relevanceScore;
    }
}