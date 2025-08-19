package com.leyue.smartcs.domain.intent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 路由类型枚举
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor
public enum RouteType {
    
    SMALL_MODEL("SMALL_MODEL", "小模型"),
    RULE("RULE", "规则引擎"),
    LLM("LLM", "大语言模型"),
    HYBRID("HYBRID", "混合模式");
    
    private final String code;
    private final String desc;
    
    public static RouteType fromCode(String code) {
        for (RouteType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的路由类型: " + code);
    }
}