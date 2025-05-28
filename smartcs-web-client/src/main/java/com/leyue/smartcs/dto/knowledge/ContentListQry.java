package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容列表查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentListQry extends PageQuery {
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 内容标题（模糊查询）
     */
    private String title;
    
    /**
     * 内容类型 document/audio/video
     */
    private String contentType;
    
    /**
     * 内容状态 uploaded/parsed/vectorized
     */
    private String status;
} 