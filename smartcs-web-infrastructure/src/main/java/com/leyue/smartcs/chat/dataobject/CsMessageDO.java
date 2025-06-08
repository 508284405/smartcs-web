package com.leyue.smartcs.chat.dataobject;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息数据对象，对应cs_message表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_cs_message", autoResultMap = true)
public class CsMessageDO extends BaseDO {
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 消息类型 0=text 1=image 2=order_card 3=system
     */
    private Integer msgType;

    /**
     * 消息种类 'USER', 'ASSISTANT', 'SYSTEM', 'TOOL'
     */
    private String chatType;
    
    /**
     * 消息内容，JSON格式存储富文本
     */
    private String content;

    /**
     * 时间戳
     */
    private Date timestamp;
}
