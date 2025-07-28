package com.leyue.smartcs.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型提供商类型枚举
 */
@Getter
@AllArgsConstructor
public enum ProviderType {

    OPENAI("openai", "OpenAI"),
    DEEPSEEK("deepseek", "DeepSeek"),
    CLAUDE("claude", "Claude"),
    GEMINI("gemini", "Gemini"),
    QWEN("qwen", "通义千问"),
    BAIDU("baidu", "百度文心"),
    TENCENT("tencent", "腾讯混元"),
    ZHIPU("zhipu", "智谱AI"),
    MODELSCOPE("modelscope", "摩登社区");

    /**
     * 提供商键值
     */
    private final String key;

    /**
     * 提供商显示名称
     */
    private final String label;

    /**
     * 根据key获取枚举
     */
    public static ProviderType fromKey(String key) {
        if (key == null) {
            return null;
        }
        for (ProviderType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查是否为OpenAI兼容的提供商
     */
    public boolean isOpenAiCompatible() {
        return this == OPENAI || this == DEEPSEEK || this == MODELSCOPE;
    }
}