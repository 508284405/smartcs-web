package com.leyue.smartcs.dto.knowledge;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 内容切片DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChunkDTO {
    
    /**
     * 切片ID
     */
    private Long id;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 段落序号
     */
    private String chunkIndex;
    
    /**
     * 切片内容文本
     */
    private String content;
    
    /**
     * 切片token数
     */
    private Integer tokenSize;
    
    /**
     * 附加元信息，如页码、起止时间、原始位置等
     */
    private String metadata;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
} 