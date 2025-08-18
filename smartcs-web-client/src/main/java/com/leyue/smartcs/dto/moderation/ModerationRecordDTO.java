package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审核记录DTO
 *
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationRecordDTO {

    /**
     * 记录ID
     */
    private String id;

    /**
     * 原始内容
     */
    private String originalContent;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 内容来源
     */
    private String source;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 审核状态
     */
    private String status;

    /**
     * 审核结果
     */
    private String result;

    /**
     * 审核方式
     */
    private String reviewType;

    /**
     * 审核员
     */
    private String reviewer;

    /**
     * 审核备注
     */
    private String reviewNotes;

    /**
     * 违规分类列表
     */
    private List<ViolationCategoryInfo> violationCategories;

    /**
     * AI审核结果
     */
    private AiModerationInfo aiResult;

    /**
     * 关键词审核结果
     */
    private KeywordModerationInfo keywordResult;

    /**
     * 审核用时（毫秒）
     */
    private Long reviewDuration;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 违规分类信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViolationCategoryInfo {
        private String categoryCode;
        private String categoryName;
        private String parentCategoryCode;
        private String parentCategoryName;
        private String severityLevel;
        private String actionType;
        private Double confidence;
    }

    /**
     * AI审核信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiModerationInfo {
        private String modelName;
        private String result;
        private Double confidence;
        private String reason;
        private List<String> detectedCategories;
        private String debugInfo;
        private Long processingTime;
    }

    /**
     * 关键词审核信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KeywordModerationInfo {
        private String result;
        private List<MatchedKeyword> matchedKeywords;
        private Long processingTime;
    }

    /**
     * 匹配的关键词信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchedKeyword {
        private String keyword;
        private String matchType;
        private String categoryCode;
        private String categoryName;
        private String actionType;
        private Double similarity;
        private Integer startPosition;
        private Integer endPosition;
    }
}