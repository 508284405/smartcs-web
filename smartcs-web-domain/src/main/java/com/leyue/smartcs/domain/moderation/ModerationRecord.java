package com.leyue.smartcs.domain.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.leyue.smartcs.domain.moderation.enums.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 内容审核记录领域实体
 * 记录每次内容审核的详细信息和结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationRecord {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 内容哈希值，用于去重和缓存
     */
    private String contentHash;

    /**
     * 原始待审核内容
     */
    private String originalContent;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 源ID，如消息ID、知识库ID等
     */
    private String sourceId;

    /**
     * 来源类型
     */
    private SourceType sourceType;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID（如果适用）
     */
    private String sessionId;

    // 审核结果相关字段
    
    /**
     * 审核结果
     */
    private ModerationResult moderationResult;

    /**
     * 风险等级
     */
    private SeverityLevel riskLevel;

    /**
     * 置信度分数 0.0000-1.0000
     */
    private BigDecimal confidenceScore;

    /**
     * 是否被阻断
     */
    private Boolean isBlocked;

    // 违规信息

    /**
     * 违规分类列表
     */
    private List<ModerationViolation> violationCategories;

    /**
     * AI分析结果
     */
    private Map<String, Object> aiAnalysisResult;

    /**
     * 匹配的关键词列表
     */
    private List<String> keywordMatches;

    // 审核方式和耗时

    /**
     * 审核方式，多个用逗号分隔
     */
    private String moderationMethods;

    /**
     * 使用的AI模型名称
     */
    private String aiModelUsed;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    // 人工审核相关

    /**
     * 人工审核状态
     */
    private String manualReviewStatus;

    /**
     * 人工审核员ID
     */
    private String manualReviewerId;

    /**
     * 人工审核备注
     */
    private String manualReviewNotes;

    /**
     * 人工审核时间
     */
    private Long manualReviewedAt;

    // 后续处理

    /**
     * 采取的行动
     */
    private ActionType actionTaken;

    /**
     * 升级给谁处理
     */
    private String escalatedTo;

    /**
     * 升级时间
     */
    private Long escalatedAt;

    // 元数据

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 请求ID，用于追踪
     */
    private String requestId;

    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    // 内部类：违规信息

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationViolation {
        /**
         * 违规分类ID
         */
        private Long categoryId;

        /**
         * 违规分类名称
         */
        private String categoryName;

        /**
         * 违规分类编码
         */
        private String categoryCode;

        /**
         * 该违规的置信度
         */
        private BigDecimal confidence;

        /**
         * 触发的具体规则信息
         */
        private String triggerRule;
    }

    // 业务方法

    /**
     * 判断审核是否通过
     */
    public boolean isPassed() {
        return ModerationResult.APPROVED.equals(moderationResult);
    }

    /**
     * 判断是否被拒绝
     */
    public boolean isRejected() {
        return ModerationResult.REJECTED.equals(moderationResult);
    }

    /**
     * 判断是否需要人工审核
     */
    public boolean needsManualReview() {
        return ModerationResult.NEEDS_REVIEW.equals(moderationResult);
    }

    /**
     * 判断是否为高风险内容
     */
    public boolean isHighRisk() {
        return riskLevel != null && riskLevel.isHighRisk();
    }

    /**
     * 获取主要违规分类
     */
    public ModerationViolation getPrimaryViolation() {
        if (violationCategories == null || violationCategories.isEmpty()) {
            return null;
        }
        
        return violationCategories.stream()
                .max((v1, v2) -> v1.getConfidence().compareTo(v2.getConfidence()))
                .orElse(violationCategories.get(0));
    }

    /**
     * 获取所有违规分类编码
     */
    public Set<String> getViolationCodes() {
        if (violationCategories == null || violationCategories.isEmpty()) {
            return Set.of();
        }
        
        return violationCategories.stream()
                .map(ModerationViolation::getCategoryCode)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 判断是否包含特定违规类型
     */
    public boolean hasViolationType(String categoryCode) {
        return getViolationCodes().contains(categoryCode);
    }

    /**
     * 获取处理耗时的用户友好格式
     */
    public String getProcessingTimeFriendly() {
        if (processingTimeMs == null) {
            return "未知";
        }
        
        if (processingTimeMs < 1000) {
            return processingTimeMs + "毫秒";
        } else {
            return String.format("%.1f秒", processingTimeMs / 1000.0);
        }
    }

    /**
     * 判断是否使用了AI审核
     */
    public boolean usedAiModeration() {
        return moderationMethods != null && moderationMethods.contains("AI");
    }

    /**
     * 判断是否使用了关键词审核
     */
    public boolean usedKeywordModeration() {
        return moderationMethods != null && moderationMethods.contains("KEYWORD");
    }

    /**
     * 判断是否经过人工审核
     */
    public boolean hasManualReview() {
        return manualReviewStatus != null && !manualReviewStatus.equals("NOT_REQUIRED");
    }

    /**
     * 更新人工审核结果
     */
    public void updateManualReview(String reviewerId, String status, String notes,
                                  ModerationResult finalResult, ActionType finalAction) {
        this.manualReviewerId = reviewerId;
        this.manualReviewStatus = status;
        this.manualReviewNotes = notes;
        this.moderationResult = finalResult;
        this.actionTaken = finalAction;
        this.manualReviewedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 升级处理
     */
    public void escalate(String escalatedTo) {
        this.escalatedTo = escalatedTo;
        this.escalatedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 创建新的审核记录（工厂方法）
     */
    public static ModerationRecord create(String contentHash, String originalContent,
                                        ContentType contentType, SourceType sourceType,
                                        String sourceId, String userId, String sessionId) {
        long currentTime = System.currentTimeMillis();
        return ModerationRecord.builder()
                .contentHash(contentHash)
                .originalContent(originalContent)
                .contentType(contentType)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .userId(userId)
                .sessionId(sessionId)
                .moderationResult(ModerationResult.PENDING)
                .isBlocked(false)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

    /**
     * 完成审核，设置结果
     */
    public void completeModeration(ModerationResult result, SeverityLevel riskLevel,
                                 BigDecimal confidence, List<ModerationViolation> violations,
                                 ActionType action, boolean blocked, long processingTime,
                                 String methods, String aiModel) {
        this.moderationResult = result;
        this.riskLevel = riskLevel;
        this.confidenceScore = confidence;
        this.violationCategories = violations;
        this.actionTaken = action;
        this.isBlocked = blocked;
        this.processingTimeMs = processingTime;
        this.moderationMethods = methods;
        this.aiModelUsed = aiModel;
        this.updatedAt = System.currentTimeMillis();
    }
}