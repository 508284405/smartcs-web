package com.leyue.smartcs.domain.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档分段策略名称枚举
 */
@Getter
public enum StrategyNameEnum {
    
    /**
     * 混合分段策略
     */
    HYBRID("Hybrid", "混合分段策略"),
    
    /**
     * 按字符数分段策略
     */
    CHAR_COUNT("CharCount", "按字符数分段策略"),
    
    /**
     * 按段落分段策略
     */
    PARAGRAPH("Paragraph", "按段落分段策略"),
    
    /**
     * 语义分段策略
     */
    SEMANTIC("Semantic", "语义分段策略"),
    
    /**
     * 按句子分段策略
     */
    SENTENCE("Sentence", "按句子分段策略");
    
    /**
     * 策略代码
     */
    private final String code;
    
    /**
     * 策略描述
     */
    private final String description;
    
    /**
     * 构造器
     *
     * @param code 策略代码
     * @param description 策略描述
     */
    StrategyNameEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取枚举
     *
     * @param code 策略代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    public static StrategyNameEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        // 处理CharCount动态名称，如 "CharCount-1000"
        if (code.startsWith("CharCount")) {
            return CHAR_COUNT;
        }
        
        for (StrategyNameEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        
        throw new IllegalArgumentException("未知的策略名称: " + code);
    }
    
    /**
     * 检查代码是否有效
     *
     * @param code 策略代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 