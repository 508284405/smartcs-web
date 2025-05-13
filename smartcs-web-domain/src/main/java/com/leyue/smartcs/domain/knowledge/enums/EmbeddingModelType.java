package com.leyue.smartcs.domain.knowledge.enums;

/**
 * Embedding模型类型枚举
 */
public enum EmbeddingModelType {
    /**
     * BGE-Large中文模型，1024维
     */
    BGE_LARGE_ZH("bge-large-zh", 1024, "中文语义效果佳"),
    
    /**
     * E5模型，1024维，支持多语言
     */
    E5_LARGE("e5-large", 1024, "多语言检索"),
    
    /**
     * MiniLM轻量模型，384维
     */
    MINI_LM_384("MiniLM-384", 384, "降低存储 & 延迟"),
    
    /**
     * OpenAI Embedding模型
     */
    OPENAI("text-embedding-ada-002", 1536, "OpenAI Embedding");
    
    /**
     * 模型标识
     */
    private final String modelId;
    
    /**
     * 向量维度
     */
    private final int dimension;
    
    /**
     * 描述
     */
    private final String description;
    
    EmbeddingModelType(String modelId, int dimension, String description) {
        this.modelId = modelId;
        this.dimension = dimension;
        this.description = description;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public int getDimension() {
        return dimension;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据模型ID获取枚举值
     * @param modelId 模型ID
     * @return 枚举值
     */
    public static EmbeddingModelType fromModelId(String modelId) {
        if (modelId == null) {
            return OPENAI; // 默认
        }
        
        for (EmbeddingModelType type : values()) {
            if (type.modelId.equals(modelId)) {
                return type;
            }
        }
        
        return OPENAI; // 默认
    }
} 