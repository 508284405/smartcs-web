package com.leyue.smartcs.dto.chat.friend;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 好友DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendDTO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 好友用户ID
     */
    private String friendUserId;
    
    /**
     * 好友用户名
     */
    private String friendUserName;
    
    /**
     * 好友头像
     */
    private String friendAvatar;
    
    /**
     * 好友备注名
     */
    private String remarkName;
    
    /**
     * 好友分组
     */
    private String friendGroup;
    
    /**
     * 好友状态 0-待审核 1-已同意 2-已拒绝 3-已拉黑
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusText;
    
    /**
     * 在线状态
     */
    private String onlineStatus;
    
    /**
     * 最后在线时间
     */
    private Long lastSeenAt;
    
    /**
     * 申请时间
     */
    private Long appliedAt;
    
    /**
     * 处理时间
     */
    private Long processedAt;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 显示名称
     */
    private String displayName;
    
    /**
     * 是否可以聊天
     */
    private Boolean canChat;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 最后消息内容
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private Long lastMessageTime;
}