package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图目录DTO
 * 
 * @author Claude
 */
@Data
public class IntentCatalogDTO {
    
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
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}