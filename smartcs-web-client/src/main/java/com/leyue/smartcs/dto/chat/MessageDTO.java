package com.leyue.smartcs.dto.chat;

import java.util.Date;

import lombok.Data;

/**
 * 消息DTO
 */
@Data
public class MessageDTO {
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
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 聊天类型
     */
    private String chatType;

    /**
     * 时间戳
     */
    private Date timestamp;

    /**
     * 是否已撤回
     */
    private Boolean isRecalled;

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
     * 是否已编辑
     */
    private Boolean isEdited;

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
     * 是否被发送者删除
     */
    private Boolean isDeletedBySender;

    /**
     * 是否被接收者删除
     */
    private Boolean isDeletedByReceiver;

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
     * 是否已读
     */
    private Boolean isRead;

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
}
