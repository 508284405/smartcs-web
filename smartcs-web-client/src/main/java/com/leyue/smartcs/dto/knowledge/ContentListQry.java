package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;

import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(uploaded|parsed|vectorized)$", message = "状态只能是uploaded、parsed或vectorized")
    private String status;
} 