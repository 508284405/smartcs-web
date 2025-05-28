package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识检索查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeSearchQry extends PageQuery {
    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 关键词查询
     */
    @NotBlank(message = "关键词不能为空")
    private String keyword;
    
    /**
     * 检索TopK结果数量
     */
    @Min(value = 1, message = "k不能小于1")
    private Integer k = 5;
    
    
    /**
     * 相似度阈值 (0-1)
     */
    private Float threshold = 0.7f;
} 