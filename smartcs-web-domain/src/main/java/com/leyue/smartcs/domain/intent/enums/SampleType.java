package com.leyue.smartcs.domain.intent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 样本类型枚举
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor
public enum SampleType {
    
    TRAIN("TRAIN", "训练样本"),
    DEV("DEV", "开发测试样本"),
    TEST("TEST", "测试样本"),
    ONLINE_HARD_NEG("ONLINE_HARD_NEG", "线上困难负样本"),
    UNKNOWN("UNKNOWN", "未知样本");
    
    private final String code;
    private final String desc;
    
    public static SampleType fromCode(String code) {
        for (SampleType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的样本类型: " + code);
    }
}