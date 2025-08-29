package com.leyue.smartcs.dto.chat.friend;

import lombok.Data;

/**
 * 添加好友命令
 */
@Data
public class AddFriendCmd {
    
    /**
     * 发起用户ID
     */
    private String fromUserId;
    
    /**
     * 目标用户ID
     */
    private String toUserId;
    
    /**
     * 申请消息
     */
    private String applyMessage;
}