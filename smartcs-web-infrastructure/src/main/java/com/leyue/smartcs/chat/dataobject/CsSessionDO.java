package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话数据对象，对应cs_session表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cs_session")
public class CsSessionDO extends BaseDO {
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 会话名称
     */
    private String sessionName;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客服ID
     */
    private Long agentId;
    
    /**
     * 会话状态 0=排队 1=进行中 2=已结束
     */
    private Integer sessionState;
    
    /**
     * 最后消息时间
     */
    private Long lastMsgTime;
}
