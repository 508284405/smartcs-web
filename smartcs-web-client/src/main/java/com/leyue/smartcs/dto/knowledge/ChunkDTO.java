package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 切片DTO
 */
@Data
public class ChunkDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 段落序号
     */
    private Integer chunkIndex;
    
    /**
     * 该段文本内容
     */
    private String text;
    
    /**
     * 切片token数
     */
    private Integer tokenSize;
    
    /**
     * 向量数据库中的ID（如Milvus主键）
     */
    private String vectorId;
    
    /**
     * 附加元信息，如页码、起止时间、原始位置等
     */
    private String metadata;
    
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