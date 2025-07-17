package com.leyue.smartcs.domain.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    
    /**
     * 数据集ID
     */
    private String id;
    
    /**
     * 索引技术：high_quality, economy 等
     */
    private String indexingTechnique;
    
    /**
     * 嵌入模型
     */
    private String embeddingModel;
    
    /**
     * 其他元数据
     */
    private java.util.Map<String, Object> metadata;
} 