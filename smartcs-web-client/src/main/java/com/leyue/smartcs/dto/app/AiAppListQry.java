package com.leyue.smartcs.dto.app;

import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * AI应用列表查询
 */
@Data
public class AiAppListQry {
    
    /**
     * 创建者ID（可选）
     */
    private Long creatorId;
    
    /**
     * 应用类型（可选）
     */
    private String type;
    
    /**
     * 应用状态（可选）
     */
    private String status;
    
    /**
     * 关键词搜索（可选）
     */
    private String keyword;
    
    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageIndex = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;
}