package com.leyue.smartcs.dto.chat;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话分页查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SessionPageQuery extends PageQuery {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客服ID
     */
    private Long agentId;
    
    /**
     * 客服名称
     */
    private String agentName;
    
    /**
     * 会话状态
     */
    private String status;
} 