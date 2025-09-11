package com.leyue.smartcs.app.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * AI应用测试会话数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_app_test_session")
public class AppTestSessionDO extends BaseDO {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * AI应用ID
     */
    private Long appId;
    
    /**
     * 会话名称
     */
    private String sessionName;
    
    /**
     * 使用的模型ID
     */
    private Long modelId;
    
    /**
     * 测试用户ID
     */
    private Long userId;
    
    /**
     * 会话配置（变量、参数等）
     */
    private String sessionConfig;
    
    /**
     * 会话状态: ACTIVE/FINISHED/EXPIRED
     */
    private String sessionState;
    
    /**
     * 消息总数
     */
    private Integer messageCount;
    
    /**
     * 最后消息时间（毫秒时间戳）
     */
    private Long lastMessageTime;
    
    /**
     * 会话开始时间（毫秒时间戳）
     */
    private Long startTime;
    
    /**
     * 会话结束时间（毫秒时间戳）
     */
    private Long endTime;
    
    /**
     * 总消耗Token数
     */
    private Integer totalTokens;
    
    /**
     * 总费用
     */
    private BigDecimal totalCost;
}