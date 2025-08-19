package com.leyue.smartcs.domain.moderation.enums;

/**
 * 内容审核严重程度级别枚举
 */
public enum SeverityLevel {
    
    /**
     * 低风险 - 轻微违规，通常给予警告即可
     */
    LOW("LOW", "低风险", 1, "轻微违规内容，通常给予警告即可"),
    
    /**
     * 中等风险 - 需要关注的违规内容，可能需要人工审核
     */
    MEDIUM("MEDIUM", "中等风险", 2, "需要关注的违规内容，建议人工审核"),
    
    /**
     * 高风险 - 明显违规内容，应当立即阻断
     */
    HIGH("HIGH", "高风险", 3, "明显违规内容，应当立即阻断"),
    
    /**
     * 极高风险 - 严重违规内容，需要立即处理并可能升级处理
     */
    CRITICAL("CRITICAL", "极高风险", 4, "严重违规内容，需要立即处理并升级");

    /**
     * 级别编码
     */
    private final String code;

    /**
     * 级别名称
     */
    private final String displayName;

    /**
     * 级别权重（用于比较和排序）
     */
    private final int weight;

    /**
     * 级别描述
     */
    private final String description;

    SeverityLevel(String code, String displayName, int weight, String description) {
        this.code = code;
        this.displayName = displayName;
        this.weight = weight;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     */
    public static SeverityLevel fromCode(String code) {
        for (SeverityLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown severity level code: " + code);
    }

    /**
     * 根据权重获取枚举值
     */
    public static SeverityLevel fromWeight(int weight) {
        for (SeverityLevel level : values()) {
            if (level.weight == weight) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown severity level weight: " + weight);
    }

    /**
     * 比较严重程度权重
     * @param other 另一个严重程度级别
     * @return 如果当前级别更严重返回正数，相等返回0，不如另一个严重返回负数
     */
    public int compareWeight(SeverityLevel other) {
        return Integer.compare(this.weight, other.weight);
    }

    /**
     * 判断是否比另一个级别更严重
     */
    public boolean isMoreSevereThan(SeverityLevel other) {
        return this.weight > other.weight;
    }

    /**
     * 判断是否比另一个级别更轻微
     */
    public boolean isLessSevereThan(SeverityLevel other) {
        return this.weight < other.weight;
    }

    /**
     * 判断是否为高风险级别（HIGH或CRITICAL）
     */
    public boolean isHighRisk() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * 判断是否为低风险级别（LOW或MEDIUM）
     */
    public boolean isLowRisk() {
        return this == LOW || this == MEDIUM;
    }

    /**
     * 获取建议的置信度阈值
     */
    public double getRecommendedThreshold() {
        switch (this) {
            case LOW:
                return 0.3;
            case MEDIUM:
                return 0.6;
            case HIGH:
                return 0.8;
            case CRITICAL:
                return 0.95;
            default:
                return 0.5;
        }
    }

    public String toDisplayString() {
        return displayName;
    }
}