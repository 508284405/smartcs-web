package com.leyue.smartcs.dto.chat;

import lombok.Data;

/**
 * 消息搜索结果
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Data
public class MessageSearchResult {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 会话标题/名称
     */
    private String sessionTitle;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 高亮后的内容（包含HTML标签）
     */
    private String highlightText;
    
    /**
     * 消息类型
     * 对应 MessageType 枚举值
     */
    private Integer messageType;
    
    /**
     * 消息类型文本描述
     */
    private String messageTypeText;
    
    /**
     * 发送者ID
     */
    private String senderId;
    
    /**
     * 发送者姓名
     */
    private String senderName;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 消息创建时间
     */
    private Long createdAt;
    
    /**
     * 匹配得分（用于相关性排序）
     */
    private Double score;
    
    /**
     * 上下文消息ID（用于跳转时定位）
     */
    private String contextMsgId;
    
    /**
     * 是否为自己发送的消息
     */
    private Boolean isMyMessage;
    
    /**
     * 消息状态
     */
    private Integer status;
    
    /**
     * 附加数据（如文件信息、位置信息等）
     */
    private String extraData;
}