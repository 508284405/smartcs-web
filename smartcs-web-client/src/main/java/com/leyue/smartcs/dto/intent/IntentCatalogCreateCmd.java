package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图目录创建命令
 * 
 * @author Claude
 */
@Data
public class IntentCatalogCreateCmd {
    
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
}