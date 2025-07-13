package com.leyue.smartcs.domain.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容状态枚举
 */
@Getter
@AllArgsConstructor
public enum ContentStatusEnum {
    
    UPLOADED("uploaded", "已上传"),
    PARSED("parsed", "已解析"),
    VECTORIZED("vectorized", "已向量化"),
    ENABLED("enabled", "启用"),
    DISABLED("disabled", "禁用");
    
    private final String code;
    private final String description;
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举实例
     */
    public static ContentStatusEnum fromCode(String code) {
        for (ContentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的内容状态: " + code);
    }
    
    /**
     * 检查代码是否有效
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (ContentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查状态是否可以转换到目标状态
     * @param target 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(ContentStatusEnum target) {
        switch (this) {
            case UPLOADED:
                return target == PARSED;
            case PARSED:
                return target == VECTORIZED;
            case VECTORIZED:
                return target == ENABLED || target == DISABLED;
            case ENABLED:
                return target == DISABLED;
            case DISABLED:
                return target == ENABLED;
            default:
                return false;
        }
    }
} 