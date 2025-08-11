package com.leyue.smartcs.domain.eval.enums;

/**
 * 评估运行类型枚举
 * 
 * @author Claude
 * @since 1.0.0
 */
public enum RunType {
    
    /**
     * 检索评估
     */
    RETRIEVAL("retrieval", "检索评估"),
    
    /**
     * 生成评估
     */
    GENERATION("generation", "生成评估"),
    
    /**
     * 端到端评估
     */
    E2E("e2e", "端到端评估"),
    
    /**
     * A/B测试
     */
    AB_TEST("ab_test", "A/B测试");
    
    private final String code;
    private final String description;
    
    RunType(String code, String description) {
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
    public static RunType fromCode(String code) {
        for (RunType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown run type code: " + code);
    }
}