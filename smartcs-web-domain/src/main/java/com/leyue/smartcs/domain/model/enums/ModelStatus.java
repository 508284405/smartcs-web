package com.leyue.smartcs.domain.model.enums;

/**
 * 模型状态枚举
 */
public enum ModelStatus {
    /**
     * 启用
     */
    ACTIVE("active", "启用"),
    
    /**
     * 禁用
     */
    INACTIVE("inactive", "禁用"),
    
    /**
     * 停用
     */
    DISABLED("disabled", "停用");
    
    private final String code;
    private final String displayName;
    
    ModelStatus(String code, String displayName) {
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
    public static ModelStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ModelStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown model status code: " + code);
    }
}