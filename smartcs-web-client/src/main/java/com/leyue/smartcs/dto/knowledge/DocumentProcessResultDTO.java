package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 文档处理结果DTO
 */
@Data
public class DocumentProcessResultDTO {
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 分块数量
     */
    private Integer chunkCount;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;
    
    /**
     * Token总数
     */
    private Integer tokenCount;
    
    /**
     * 嵌入成本
     */
    private Integer embeddingCost;
    
    /**
     * 字符数
     */
    private Long charCount;
    
    /**
     * 召回次数
     */
    private Long recallCount;
    
    /**
     * 处理状态
     */
    private String status;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
} 