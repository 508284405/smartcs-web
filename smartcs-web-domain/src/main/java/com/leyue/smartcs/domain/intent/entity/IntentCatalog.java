package com.leyue.smartcs.domain.intent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 意图目录实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentCatalog {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 目录名称
     */
    private String name;
    
    /**
     * 目录编码
     */
    private String code;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 父目录ID
     */
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 意图数量
     */
    private Integer intentCount;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 是否删除
     */
    private Boolean isDeleted;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}