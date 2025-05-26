package com.leyue.smartcs.dto.data;

import lombok.Data;

/**
 * 客服数据传输对象
 */
@Data
public class AgentServiceDTO {
    
    /**
     * 客服ID
     */
    private String serviceId;
    
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
     * 总会话数
     */
    private Integer totalSessions;
    
    /**
     * 最后活跃时间
     */
    private Long lastActiveTime;
    
    /**
     * 操作
     */
    private String operation;
} 