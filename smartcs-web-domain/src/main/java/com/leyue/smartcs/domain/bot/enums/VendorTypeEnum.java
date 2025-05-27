package com.leyue.smartcs.domain.bot.enums;

/**
 * AI模型厂商类型枚举
 */
public enum VendorTypeEnum {
    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI"),
    
    /**
     * DeepSeek
     */
    DEEPSEEK("deepseek", "DeepSeek"),
    
    /**
     * 智谱AI
     */
    ZHIPU("zhipu", "智谱AI"),
    
    /**
     * 月之暗面
     */
    MOONSHOT("moonshot", "月之暗面"),
    
    /**
     * 百度文心
     */
    BAIDU("baidu", "百度文心"),
    
    /**
     * 阿里通义
     */
    ALIBABA("alibaba", "阿里通义"),
    
    /**
     * 腾讯混元
     */
    TENCENT("tencent", "腾讯混元");
    
    /**
     * 厂商标识
     */
    private final String code;
    
    /**
     * 厂商名称
     */
    private final String displayName;
    
    VendorTypeEnum(String code, String displayName) {
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
     * @param code 厂商代码
     * @return 枚举值
     */
    public static VendorTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (VendorTypeEnum vendor : values()) {
            if (vendor.code.equals(code)) {
                return vendor;
            }
        }
        
        throw new IllegalArgumentException("Unknown vendor code: " + code);
    }
} 