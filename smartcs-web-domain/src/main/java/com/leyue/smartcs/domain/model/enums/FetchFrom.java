package com.leyue.smartcs.domain.model.enums;

/**
 * 模型来源枚举
 */
public enum FetchFrom {
    /**
     * 预定义模型
     */
    PREDEFINED_MODEL("predefined-model", "预定义模型"),
    
    /**
     * 自定义模型
     */
    CUSTOM_MODEL("custom-model", "自定义模型");
    
    private final String code;
    private final String displayName;
    
    FetchFrom(String code, String displayName) {
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
     */
    public static FetchFrom fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (FetchFrom fetchFrom : values()) {
            if (fetchFrom.code.equals(code)) {
                return fetchFrom;
            }
        }
        
        throw new IllegalArgumentException("Unknown fetch from code: " + code);
    }
}