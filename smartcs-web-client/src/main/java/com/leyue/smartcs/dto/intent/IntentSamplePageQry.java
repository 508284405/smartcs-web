package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图样本分页查询
 * 
 * @author Claude
 */
@Data
public class IntentSamplePageQry {
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 样本类型
     */
    private String type;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}