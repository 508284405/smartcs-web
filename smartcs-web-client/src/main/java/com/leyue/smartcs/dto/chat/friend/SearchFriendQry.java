package com.leyue.smartcs.dto.chat.friend;

import lombok.Data;

/**
 * 搜索好友查询
 */
@Data
public class SearchFriendQry {
    
    /**
     * 搜索用户ID
     */
    private String userId;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 好友分组过滤
     */
    private String friendGroup;
    
    /**
     * 在线状态过滤
     */
    private String onlineStatus;
}