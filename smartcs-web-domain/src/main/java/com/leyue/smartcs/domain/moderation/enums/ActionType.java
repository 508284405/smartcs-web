package com.leyue.smartcs.domain.moderation.enums;

/**
 * 内容审核处理动作类型枚举
 */
public enum ActionType {
    
    /**
     * 审核通过 - 内容完全合规，允许正常使用
     */
    APPROVE("APPROVE", "审核通过", 0, "内容完全合规，允许正常使用"),
    
    /**
     * 警告 - 记录违规但允许内容通过，给用户警告提示
     */
    WARN("WARN", "警告", 1, "记录违规并给用户警告，允许内容通过"),
    
    /**
     * 人工审核 - 标记为需要人工审核，暂时阻断等待审核结果
     */
    REVIEW("REVIEW", "人工审核", 2, "提交人工审核，暂时阻断等待审核结果"),
    
    /**
     * 阻断 - 直接阻断违规内容，不允许通过
     */
    BLOCK("BLOCK", "阻断", 3, "直接阻断违规内容，不允许通过"),
    
    /**
     * 升级处理 - 升级给管理员或专门团队处理，同时阻断内容
     */
    ESCALATE("ESCALATE", "升级处理", 4, "升级给管理员处理，同时阻断内容");

    /**
     * 动作编码
     */
    private final String code;

    /**
     * 动作名称
     */
    private final String displayName;

    /**
     * 动作严厉程度（用于比较和排序）
     */
    private final int severity;

    /**
     * 动作描述
     */
    private final String description;

    ActionType(String code, String displayName, int severity, String description) {
        this.code = code;
        this.displayName = displayName;
        this.severity = severity;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     */
    public static ActionType fromCode(String code) {
        for (ActionType action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action type code: " + code);
    }

    /**
     * 根据严厉程度获取枚举值
     */
    public static ActionType fromSeverity(int severity) {
        for (ActionType action : values()) {
            if (action.severity == severity) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action type severity: " + severity);
    }

    /**
     * 比较动作严厉程度
     * @param other 另一个动作类型
     * @return 如果当前动作更严厉返回正数，相等返回0，不如另一个严厉返回负数
     */
    public int compareSeverity(ActionType other) {
        return Integer.compare(this.severity, other.severity);
    }

    /**
     * 判断是否比另一个动作更严厉
     */
    public boolean isMoreSevereThan(ActionType other) {
        return this.severity > other.severity;
    }

    /**
     * 判断是否比另一个动作更宽松
     */
    public boolean isMoreLenientThan(ActionType other) {
        return this.severity < other.severity;
    }

    /**
     * 判断是否为阻断性动作（BLOCK或ESCALATE）
     */
    public boolean isBlocking() {
        return this == BLOCK || this == ESCALATE;
    }

    /**
     * 判断是否为非阻断性动作（APPROVE、WARN或REVIEW）
     */
    public boolean isNonBlocking() {
        return this == APPROVE || this == WARN || this == REVIEW;
    }

    /**
     * 判断是否需要人工介入（REVIEW或ESCALATE）
     */
    public boolean requiresHumanIntervention() {
        return this == REVIEW || this == ESCALATE;
    }

    /**
     * 根据风险级别推荐合适的动作类型
     */
    public static ActionType recommendForSeverityLevel(SeverityLevel severityLevel) {
        switch (severityLevel) {
            case LOW:
                return APPROVE;
            case MEDIUM:
                return WARN;
            case HIGH:
                return REVIEW;
            case CRITICAL:
                return BLOCK;
            default:
                return BLOCK;
        }
    }

    /**
     * 获取用户友好的动作描述
     */
    public String getUserFriendlyDescription() {
        switch (this) {
            case APPROVE:
                return "您的内容完全合规，可以正常使用";
            case WARN:
                return "您的内容包含轻微不当内容，请注意用词规范";
            case REVIEW:
                return "您的内容需要人工审核，请耐心等待";
            case BLOCK:
                return "您的内容违反了社区规范，无法发布";
            case ESCALATE:
                return "您的内容存在严重违规，已提交给管理员处理";
            default:
                return "内容审核中";
        }
    }

    public String toDisplayString() {
        return displayName;
    }
}