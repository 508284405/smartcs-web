package com.leyue.smartcs.chat.database.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

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
    private Long msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者角色 0=用户 1=客服 2=机器人
     */
    private Integer senderRole;
    
    /**
     * 消息类型 0=text 1=image 2=order_card 3=system
     */
    private Integer msgType;
    
    /**
     * 消息内容，JSON格式存储富文本
     */
    private String content;
    
    /**
     * @提及的用户列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> atList;
}
