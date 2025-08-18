package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图目录更新命令
 * 
 * @author Claude
 */
@Data
public class IntentCatalogUpdateCmd {
    
    /**
     * 目录ID
     */
    private Long catalogId;
    
    /**
     * 目录名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sortOrder;
}