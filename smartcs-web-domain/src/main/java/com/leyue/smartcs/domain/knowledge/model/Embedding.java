package com.leyue.smartcs.domain.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量嵌入领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Embedding {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 段落序号
     */
    private Integer sectionIdx;
    
    /**
     * 文本片段
     */
    private String contentSnip;
    
    /**
     * 向量数据（Base64编码字符串或字节数组）
     */
    private Object vector;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 获取文本摘要，用于预览
     * @param maxLength 最大长度
     * @return 文本摘要
     */
    public String getContentSummary(int maxLength) {
        if (contentSnip == null || contentSnip.length() <= maxLength) {
            return contentSnip;
        }
        return contentSnip.substring(0, maxLength) + "...";
    }
    
    /**
     * 是否有效向量
     * @return 是否有效
     */
    public boolean isValidVector() {
        return vector != null;
    }
} 