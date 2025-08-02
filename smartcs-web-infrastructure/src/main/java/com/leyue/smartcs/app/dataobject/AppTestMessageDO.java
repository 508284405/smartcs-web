package com.leyue.smartcs.app.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * AI应用测试消息数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_app_test_message")
public class AppTestMessageDO extends BaseDO {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * AI应用ID
     */
    private Long appId;
    
    /**
     * 消息类型: USER/ASSISTANT/SYSTEM
     */
    private String messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息变量（用户消息时使用）
     */
    private String variables;
    
    /**
     * 模型信息（AI回复时使用）
     */
    private String modelInfo;
    
    /**
     * Token使用情况
     */
    private String tokenUsage;
    
    /**
     * 处理时间（毫秒）
     */
    private Integer processTime;
    
    /**
     * 消息费用
     */
    private BigDecimal cost;
    
    /**
     * 消息状态: SUCCESS/FAILED/PROCESSING
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 消息时间戳（毫秒）
     */
    private Long timestamp;
}