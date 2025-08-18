package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图分页查询
 * 
 * @author Claude
 */
@Data
public class IntentPageQry {
    
    /**
     * 目录ID
     */
    private Long catalogId;
    
    /**
     * 状态
     */
    private String status;
    
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