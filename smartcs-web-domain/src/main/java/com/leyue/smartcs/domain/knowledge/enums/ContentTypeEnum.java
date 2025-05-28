package com.leyue.smartcs.domain.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容类型枚举
 */
@Getter
@AllArgsConstructor
public enum ContentTypeEnum {
    
    DOCUMENT("document", "文档"),
    AUDIO("audio", "音频"),
    VIDEO("video", "视频");
    
    private final String code;
    private final String description;
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举实例
     */
    public static ContentTypeEnum fromCode(String code) {
        for (ContentTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的内容类型: " + code);
    }
    
    /**
     * 检查代码是否有效
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (ContentTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
} 