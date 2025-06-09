package com.leyue.smartcs.dto.knowledge;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 文档向量搜索结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResultDTO {
    
    /**
     * 文档ID
     */
    private String id;
    
    /**
     * 文档文本内容
     */
    private String text;
    
    /**
     * 相似度分数
     */
    private Double score;
    
    /**
     * 文档元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 关联的切片信息
     */
    private ChunkDTO chunkInfo;
}