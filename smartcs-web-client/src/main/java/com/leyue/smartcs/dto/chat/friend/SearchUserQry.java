package com.leyue.smartcs.dto.chat.friend;

import lombok.Data;

/**
 * 搜索用户查询（用于添加好友）
 */
@Data
public class SearchUserQry {
    
    /**
     * 搜索关键词（用户ID、昵称、邮箱等）
     */
    private String keyword;
    
    /**
     * 搜索发起用户ID
     */
    private String searchUserId;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 页大小
     */
    private Integer size = 20;
}