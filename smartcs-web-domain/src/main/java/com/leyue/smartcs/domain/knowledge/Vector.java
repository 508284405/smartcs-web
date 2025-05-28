package com.leyue.smartcs.domain.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vector {
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
    
    /**
     * 检查向量是否有效
     * @return 是否有效
     */
    public boolean isValidVector() {
        return this.embedding != null && this.embedding.length > 0 && this.dim != null && this.dim > 0;
    }
    
    /**
     * 检查维度是否匹配
     * @param expectedDim 期望的维度
     * @return 是否匹配
     */
    public boolean isDimensionMatch(int expectedDim) {
        return this.dim != null && this.dim.equals(expectedDim);
    }
    
    /**
     * 检查提供方是否有效
     * @return 是否有效
     */
    public boolean isValidProvider() {
        return this.provider != null && !this.provider.trim().isEmpty();
    }
    
    /**
     * 获取向量维度信息
     * @return 维度描述
     */
    public String getDimensionInfo() {
        return String.format("维度: %d, 提供方: %s", this.dim, this.provider);
    }
} 