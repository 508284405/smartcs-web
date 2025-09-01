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

    /**
     * 是否已撤回 0-未撤回 1-已撤回
     */
    private Integer isRecalled;

    /**
     * 撤回时间戳
     */
    private Long recalledAt;

    /**
     * 撤回操作者ID
     */
    private String recalledBy;

    /**
     * 撤回原因
     */
    private String recallReason;

    /**
     * 是否被发送者删除 0-未删除 1-已删除
     */
    private Integer isDeletedBySender;

    /**
     * 是否被接收者删除 0-未删除 1-已删除
     */
    private Integer isDeletedByReceiver;

    /**
     * 发送者删除时间戳
     */
    private Long deletedBySenderAt;

    /**
     * 接收者删除时间戳
     */
    private Long deletedByReceiverAt;

    /**
     * 删除类型 0-仅自己可见删除 1-双方删除
     */
    private Integer deleteType;

    /**
     * 删除原因
     */
    private String deletedReason;

    /**
     * 是否已编辑 0-未编辑 1-已编辑
     */
    private Integer isEdited;

    /**
     * 编辑时间戳
     */
    private Long editedAt;

    /**
     * 原始内容（用于编辑历史）
     */
    private String originalContent;

    /**
     * 编辑次数
     */
    private Integer editCount;

    /**
     * 是否已读 0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 读取时间戳
     */
    private Long readAt;

    /**
     * 读取者ID
     */
    private String readBy;

    /**
     * 消息发送状态 0-发送中 1-已送达 2-发送失败 3-已读
     */
    private Integer sendStatus;

    /**
     * 发送失败原因
     */
    private String sendFailReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 回复的消息ID
     */
    private String replyToMsgId;

    /**
     * 被回复的消息内容（用于显示引用）
     */
    private String quotedContent;

    /**
     * 被回复的消息发送者
     */
    private String quotedFromUser;

    /**
     * 表情反应总数
     */
    private Integer reactionCount;

    /**
     * 表情反应摘要统计（JSON格式）
     */
    private String reactionsSummary;
}
