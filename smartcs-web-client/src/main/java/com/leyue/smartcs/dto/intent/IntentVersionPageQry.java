package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图版本分页查询
 * 
 * @author Claude
 */
@Data
public class IntentVersionPageQry {
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}