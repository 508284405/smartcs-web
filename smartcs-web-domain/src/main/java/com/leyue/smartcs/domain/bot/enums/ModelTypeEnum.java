package com.leyue.smartcs.domain.bot.enums;

/**
 * AI模型类型枚举
 */
public enum ModelTypeEnum {
    /**
     * 对话模型
     */
    CHAT("chat", "对话模型"),
    
    /**
     * 向量模型
     */
    EMBEDDING("embedding", "向量模型"),
    
    /**
     * 图像模型
     */
    IMAGE("image", "图像模型"),
    
    /**
     * 音频模型
     */
    AUDIO("audio", "音频模型"),
    
    /**
     * 视频模型
     */
    VIDEO("video", "视频模型");
    
    /**
     * 模型类型标识
     */
    private final String code;
    
    /**
     * 模型类型名称
     */
    private final String displayName;
    
    ModelTypeEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 根据代码获取枚举值
     * @param code 模型类型代码
     * @return 枚举值
     */
    public static ModelTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ModelTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown model type code: " + code);
    }
} 