package com.leyue.smartcs.domain.eval.enums;

/**
 * 评估指标类别枚举
 * 
 * @author Claude
 * @since 1.0.0
 */
public enum MetricCategory {
    
    /**
     * 检索指标
     */
    RETRIEVAL("retrieval", "检索指标"),
    
    /**
     * 生成指标
     */
    GENERATION("generation", "生成指标"),
    
    /**
     * 效率指标
     */
    EFFICIENCY("efficiency", "效率指标"),
    
    /**
     * 鲁棒性指标
     */
    ROBUSTNESS("robustness", "鲁棒性指标");
    
    private final String code;
    private final String description;
    
    MetricCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     */
    public static MetricCategory fromCode(String code) {
        for (MetricCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown metric category code: " + code);
    }
}