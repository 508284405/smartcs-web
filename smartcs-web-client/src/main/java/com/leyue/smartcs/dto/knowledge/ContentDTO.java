package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 内容DTO
 */
@Data
public class ContentDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 所属知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容类型 document/audio/video
     */
    private String contentType;
    
    /**
     * 原始文件地址
     */
    private String fileUrl;

    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 提取后的原始文本
     */
    private String textExtracted;
    
    /**
     * 状态 uploaded/parsed/vectorized/enabled/disabled
     */
    private String status;

    /**
     * 分段模式 general/parent_child
     */
    private String segmentMode;

    /**
     * 字符数
     */
    private Long charCount;

    /**
     * 召回次数
     */
    private Long recallCount;
    
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
     * 元数据信息（JSON格式）
     */
    private String metadata;

    /**
     * 原始文件名称
     */
    private String originalFileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 来源 upload/api/import
     */
    private String source;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;

    /**
     * 向量化时间（毫秒）
     */
    private Long embeddingTime;

    /**
     * 嵌入成本（tokens）
     */
    private Long embeddingCost;

    /**
     * 平均段落长度
     */
    private Integer averageChunkLength;

    /**
     * 段落数量
     */
    private Integer chunkCount;

    /**
     * 处理状态 processing/success/failed
     */
    private String processingStatus;

    /**
     * 处理错误信息
     */
    private String processingErrorMessage;

    /**
     * 召回率百分比
     */
    private Double recallRate;
} 