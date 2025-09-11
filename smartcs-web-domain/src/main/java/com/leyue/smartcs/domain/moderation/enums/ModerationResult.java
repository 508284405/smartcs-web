package com.leyue.smartcs.domain.moderation.enums;

/**
 * 内容审核结果枚举
 */
public enum ModerationResult {
    
    /**
     * 审核通过 - 内容合规，允许通过
     */
    APPROVED("APPROVED", "审核通过", "内容合规，允许通过"),
    
    /**
     * 审核拒绝 - 内容违规，不允许通过
     */
    REJECTED("REJECTED", "审核拒绝", "内容违规，不允许通过"),
    
    /**
     * 需要审核 - 内容可疑，需要人工审核
     */
    NEEDS_REVIEW("NEEDS_REVIEW", "需要审核", "内容可疑，需要人工审核"),
    
    /**
     * 审核中 - 正在处理中
     */
    PENDING("PENDING", "审核中", "正在处理审核中");

    /**
     * 结果编码
     */
    private final String code;

    /**
     * 结果名称
     */
    private final String displayName;

    /**
     * 结果描述
     */
    private final String description;

    ModerationResult(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     */
    public static ModerationResult fromCode(String code) {
        for (ModerationResult result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown moderation result code: " + code);
    }

    /**
     * 判断是否为通过状态
     */
    public boolean isApproved() {
        return this == APPROVED;
    }

    /**
     * 判断是否为拒绝状态
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * 判断是否需要进一步处理
     */
    public boolean needsFurtherProcessing() {
        return this == NEEDS_REVIEW || this == PENDING;
    }

    /**
     * 判断是否为最终状态（不需要进一步处理）
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
}