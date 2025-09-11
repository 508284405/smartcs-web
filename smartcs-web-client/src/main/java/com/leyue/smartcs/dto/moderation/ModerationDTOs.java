package com.leyue.smartcs.dto.moderation;

import com.alibaba.cola.dto.PageQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 审核相关DTO类集合
 * 包含所有审核模块相关的DTO、Command和Query对象
 *
 * @author Claude
 */
public class ModerationDTOs {

    // ===== Page Query Objects =====

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ModerationCategoryPageQry extends PageQuery {
        private String name;
        private String code;
        private Long parentId;
        private String severityLevel;
        private String actionType;
        private Boolean isActive;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ModerationKeywordRulePageQry extends PageQuery {
        private String name;
        private String keyword;
        private String matchType;
        private String categoryCode;
        private String actionType;
        private Boolean enabled;
        private String language;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ModerationStatisticsTrendsQry extends PageQuery {
        private String timeRange;
        private String granularity;
        private String metricType;
        private Long startTime;
        private Long endTime;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ModerationReviewerStatQry extends PageQuery {
        private String reviewerId;
        private String timeRange;
        private Long startTime;
        private Long endTime;
    }

    // ===== Command Objects =====

    @Data
    public static class ModerationCategoryCreateCmd {
        @NotBlank(message = "分类名称不能为空")
        private String name;

        @NotBlank(message = "分类编码不能为空")
        private String code;

        private String description;

        private Long parentId;

        @NotBlank(message = "严重程度级别不能为空")
        private String severityLevel;

        @NotBlank(message = "处理动作类型不能为空")
        private String actionType;

        private Integer sortOrder;

        private Boolean isActive = true;
    }

    @Data
    public static class ModerationCategoryUpdateCmd {
        private Long id;

        @NotBlank(message = "分类名称不能为空")
        private String name;

        private String description;

        @NotBlank(message = "严重程度级别不能为空")
        private String severityLevel;

        @NotBlank(message = "处理动作类型不能为空")
        private String actionType;

        private Integer sortOrder;

        private Boolean isActive;
    }

    @Data
    public static class ModerationKeywordRuleCreateCmd {
        @NotBlank(message = "规则名称不能为空")
        private String name;

        @NotBlank(message = "关键词不能为空")
        private String keyword;

        @NotBlank(message = "匹配类型不能为空")
        private String matchType;

        @NotBlank(message = "违规分类不能为空")
        private String categoryCode;

        @NotBlank(message = "处理动作不能为空")
        private String actionType;

        private Double similarityThreshold;
        private String language;
        private Boolean enabled;
        private Integer priority;
        private String description;
        private List<String> tags;
        private Long effectiveTime;
        private Long expireTime;
    }

    @Data
    public static class ModerationKeywordRuleUpdateCmd {
        private Long ruleId;

        @NotBlank(message = "规则名称不能为空")
        private String name;

        @NotBlank(message = "关键词不能为空")
        private String keyword;

        @NotBlank(message = "匹配类型不能为空")
        private String matchType;

        @NotBlank(message = "违规分类不能为空")
        private String categoryCode;

        @NotBlank(message = "处理动作不能为空")
        private String actionType;

        private Double similarityThreshold;
        private String language;
        private Boolean enabled;
        private Integer priority;
        private String description;
        private List<String> tags;
        private Long effectiveTime;
        private Long expireTime;
    }

    @Data
    public static class ModerationKeywordTestCmd {
        @NotBlank(message = "测试内容不能为空")
        private String content;

        private String language;
        private List<Long> ruleIds;
        private Boolean enableSimilarity;
        private Double similarityThreshold;
    }

    @Data
    public static class ModerationConfigUpdateCmd {
        private Boolean aiModerationEnabled;
        private Boolean keywordModerationEnabled;
        private String defaultAction;
        private Double aiConfidenceThreshold;
        private Integer maxProcessingTime;
        private Map<String, Object> aiModelConfig;
        private Map<String, Object> keywordConfig;
        private List<String> whitelist;
        private List<String> blacklist;
    }

    @Data
    public static class ModerationTestCmd {
        @NotBlank(message = "测试内容不能为空")
        private String content;

        private String contentType;
        private String language;
        private Boolean enableAi;
        private Boolean enableKeyword;
        private List<String> categoryFilters;
    }

    @Data
    public static class ModerationBatchTestCmd {
        @NotEmpty(message = "测试内容列表不能为空")
        private List<String> contents;

        private String contentType;
        private String language;
        private Boolean enableAi;
        private Boolean enableKeyword;
        private List<String> categoryFilters;
    }

    // ===== Response/Result DTOs =====

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationCategoryDTO {
        private Long id;
        private Long parentId;
        private String name;
        private String code;
        private String description;
        private String severityLevel;
        private String actionType;
        private Boolean isActive;
        private Integer sortOrder;
        private String createdBy;
        private String updatedBy;
        private Long createdAt;
        private Long updatedAt;
        private List<ModerationCategoryDTO> children;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationTestResultDTO {
        private String content;
        private String result;
        private String riskLevel;
        private Double confidence;
        private List<String> violationCategories;
        private List<MatchedKeywordInfo> matchedKeywords;
        private String reason;
        private String suggestedAction;
        private Long processingTime;
        private Map<String, Object> debugInfo;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MatchedKeywordInfo {
            private String keyword;
            private String matchType;
            private String categoryCode;
            private String categoryName;
            private Double similarity;
            private Integer position;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationConfigDTO {
        private Boolean aiModerationEnabled;
        private Boolean keywordModerationEnabled;
        private String defaultAction;
        private Double aiConfidenceThreshold;
        private Integer maxProcessingTime;
        private Map<String, Object> aiModelConfig;
        private Map<String, Object> keywordConfig;
        private List<String> whitelist;
        private List<String> blacklist;
        private Long updateTime;
        private String updateBy;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationStatisticsDTO {
        private Long totalRecords;
        private Long todayRecords;
        private Long approvedRecords;
        private Long rejectedRecords;
        private Long pendingRecords;
        private Double approvalRate;
        private Double rejectionRate;
        private Long avgProcessingTime;
        private Map<String, Long> categoryStats;
        private Map<String, Long> riskLevelStats;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationTrendsDTO {
        private List<TrendPoint> totalTrend;
        private List<TrendPoint> approvedTrend;
        private List<TrendPoint> rejectedTrend;
        private List<TrendPoint> processingTimeTrend;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TrendPoint {
            private String timePoint;
            private Long value;
            private Double percentage;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationCategoryStatDTO {
        private String categoryCode;
        private String categoryName;
        private Long violationCount;
        private Double percentage;
        private String severityLevel;
        private Long avgProcessingTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationReviewerStatDTO {
        private String reviewerId;
        private String reviewerName;
        private Long totalReviews;
        private Long approvedCount;
        private Long rejectedCount;
        private Double approvalRate;
        private Long avgProcessingTime;
        private Double efficiency;
    }
}