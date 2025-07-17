package com.leyue.smartcs.domain.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检索参数配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveParams {
    
    /**
     * 检索方法
     */
    private String retrievalMethod;
    
    /**
     * 查询字符串
     */
    private String query;
    
    /**
     * 数据集
     */
    private Dataset dataset;
    
    /**
     * Top K
     */
    private int topK;
    
    /**
     * 分数阈值
     */
    private float scoreThreshold;
    
    /**
     * 重排序模型配置
     */
    private java.util.Map<String, Object> rerankingModel;
} 