package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 向量DTO
 */
@Data
public class VectorDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 切片ID
     */
    private Long chunkId;
    
    /**
     * 向量数据，float[]序列化后存储
     */
    private byte[] embedding;
    
    /**
     * 维度大小
     */
    private Integer dim;
    
    /**
     * embedding提供方，如openai/bge
     */
    private String provider;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
} 