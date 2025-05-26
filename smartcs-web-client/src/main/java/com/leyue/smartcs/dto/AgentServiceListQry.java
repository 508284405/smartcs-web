package com.leyue.smartcs.dto;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服列表查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentServiceListQry extends PageQuery {
    
    /**
     * 客服姓名
     */
    private String serviceName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 活跃会话数
     */
    private Integer activeSessions;
    
    /**
     * 最后活跃时间开始
     */
    private Long lastActiveTimeStart;
    
    /**
     * 最后活跃时间结束
     */
    private Long lastActiveTimeEnd;
} 