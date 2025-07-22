package com.leyue.smartcs.domain.model.enums;

/**
 * 模型类型枚举
 */
public enum ModelType {
    /**
     * 大语言模型
     */
    LLM("llm", "大语言模型"),
    
    /**
     * 文本转语音
     */
    TTS("tts", "文本转语音"),
    
    /**
     * 文本向量化
     */
    TEXT_EMBEDDING("text-embedding", "文本向量化"),
    
    /**
     * 重排模型
     */
    RERANK("rerank", "重排模型"),
    
    /**
     * 语音转文本
     */
    SPEECH2TEXT("speech2text", "语音转文本");
    
    private final String code;
    private final String displayName;
    
    ModelType(String code, String displayName) {
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
    public static ModelType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ModelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown model type code: " + code);
    }
}