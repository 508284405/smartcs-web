package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图目录列表查询
 * 
 * @author Claude
 */
@Data
public class IntentCatalogListQry {
    
    /**
     * 父目录ID
     */
    private Long parentId;
}