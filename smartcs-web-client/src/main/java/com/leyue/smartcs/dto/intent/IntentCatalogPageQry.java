package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图目录分页查询
 * 
 * @author Claude
 */
@Data
public class IntentCatalogPageQry {
    
    /**
     * 父级目录ID
     */
    private Long parentId;
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}