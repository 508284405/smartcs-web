package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户状态变更消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserStatusMessage extends WebSocketMessage {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户昵称
     */
    private String userName;
    
    /**
     * 用户状态：online, offline, busy, away, invisible
     */
    private String status;
    
    /**
     * 最后在线时间戳
     */
    private Long lastSeenAt;
    
    /**
     * 状态消息（可选的自定义状态文本）
     */
    private String statusMessage;
    
    public UserStatusMessage() {
        super.setType("USER_STATUS");
    }
}