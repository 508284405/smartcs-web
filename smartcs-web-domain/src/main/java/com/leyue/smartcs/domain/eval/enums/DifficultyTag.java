package com.leyue.smartcs.domain.eval.enums;

/**
 * 难度标签枚举
 * 
 * @author Claude
 * @since 1.0.0
 */
public enum DifficultyTag {
    
    /**
     * 简单
     */
    EASY("easy", "简单"),
    
    /**
     * 中等
     */
    MEDIUM("medium", "中等"),
    
    /**
     * 困难
     */
    HARD("hard", "困难");
    
    private final String code;
    private final String description;
    
    DifficultyTag(String code, String description) {
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
    public static DifficultyTag fromCode(String code) {
        for (DifficultyTag tag : values()) {
            if (tag.code.equals(code)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty tag code: " + code);
    }
}