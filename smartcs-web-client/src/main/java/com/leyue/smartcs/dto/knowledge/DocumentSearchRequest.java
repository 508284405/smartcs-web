package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * 文档向量搜索请求DTO
 */
@Data
public class DocumentSearchRequest {
    
    /**
     * 自然语言查询
     */
    @NotBlank(message = "查询内容不能为空")
    private String query;
    
    /**
     * 返回结果数量
     */
    @Min(value = 1, message = "topK不能小于1")
    @Max(value = 100, message = "topK不能大于100")
    private Integer topK = 5;
    
    /**
     * 温度参数，用于控制搜索的随机性 (0.0-1.0)
     */
    @Min(value = 0, message = "temperature不能小于0")
    @Max(value = 1, message = "temperature不能大于1")
    private Double temperature = 0.7;
    
    /**
     * 相似度阈值 (0.0-1.0)
     */
    @Min(value = 0, message = "similarityThreshold不能小于0")
    @Max(value = 1, message = "similarityThreshold不能大于1")
    private Double similarityThreshold = 0.7;
    
    /**
     * 内容ID，可选，指定搜索特定内容
     */
    private Long contentId;
}