package com.leyue.smartcs.client.ltm.dto.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 语义记忆DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticMemoryDTO {

    /**
     * 记忆ID
     */
    private Long id;

    /**
     * 概念名称
     */
    private String concept;

    /**
     * 知识描述
     */
    private String knowledge;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 支持证据数量
     */
    private Integer evidenceCount;

    /**
     * 矛盾证据数量
     */
    private Integer contradictionCount;

    /**
     * 最后强化时间
     */
    private Long lastReinforcedAt;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;

    /**
     * 来源情景记忆
     */
    private List<SourceEpisodeDTO> sourceEpisodes;

    /**
     * 相关概念
     */
    private List<RelatedConceptDTO> relatedConcepts;

    /**
     * 知识分类
     */
    private String category;

    /**
     * 知识重要性等级
     */
    private ImportanceLevel importanceLevel;

    /**
     * 是否为争议性知识
     */
    private Boolean isControversial;

    /**
     * 用户验证状态
     */
    private VerificationStatus verificationStatus;

    /**
     * 应用次数
     */
    private Integer applicationCount;

    /**
     * 用户评价
     */
    private UserRating userRating;

    /**
     * 重要性等级
     */
    public enum ImportanceLevel {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("关键");

        private final String description;

        ImportanceLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 验证状态
     */
    public enum VerificationStatus {
        UNVERIFIED("未验证"),
        USER_CONFIRMED("用户确认"),
        USER_REJECTED("用户否认"),
        AUTO_VALIDATED("自动验证");

        private final String description;

        VerificationStatus(String description) {
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
    public static class SourceEpisodeDTO {
        private String episodeId;
        private String contentSummary;
        private Long timestamp;
        private Double contributionWeight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedConceptDTO {
        private String concept;
        private Double similarity;
        private String relationType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRating {
        private Integer rating; // 1-5星
        private String comment;
        private Long ratedAt;
    }
}