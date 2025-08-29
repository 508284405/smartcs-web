package com.leyue.smartcs.dto.chat.friend;

import lombok.Data;

/**
 * 更新好友信息命令
 */
@Data
public class UpdateFriendCmd {
    
    /**
     * 好友关系ID
     */
    private Long friendId;
    
    /**
     * 操作用户ID
     */
    private String userId;
    
    /**
     * 好友备注名
     */
    private String remarkName;
    
    /**
     * 好友分组
     */
    private String friendGroup;
}