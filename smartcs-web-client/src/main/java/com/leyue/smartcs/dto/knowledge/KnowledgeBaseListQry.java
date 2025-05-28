package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库列表查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseListQry extends PageQuery {
    
    /**
     * 知识库名称（模糊查询）
     */
    private String name;
    
    /**
     * 可见性过滤 public/private
     */
    private String visibility;
    
    /**
     * 创建者ID
     */
    private Long ownerId;
} 