package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 切片列表查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChunkListQry extends PageQuery {
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 切片内容关键词搜索
     */
    private String keyword;
    
    /**
     * 段落序号
     */
    private Integer chunkIndex;
} 