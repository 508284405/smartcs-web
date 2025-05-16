package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识检索查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeSearchQry extends PageQuery {
    /**
     * 关键词查询
     */
    private String keyword;
    
    /**
     * 向量查询（Base64编码）
     */
    private String vector;
    
    /**
     * 检索TopK结果数量
     */
    private Integer k = 5;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * 相似度阈值 (0-1)
     */
    private Float threshold = 0.7f;
} 