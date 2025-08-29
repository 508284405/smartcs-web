package com.leyue.smartcs.dto.chat.friend;

import lombok.Data;

/**
 * 处理好友申请命令
 */
@Data
public class ProcessFriendApplicationCmd {
    
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 操作类型: accept, reject, block
     */
    private String action;
    
    /**
     * 处理者用户ID
     */
    private String processedBy;
    
    /**
     * 拒绝原因（仅拒绝时需要）
     */
    private String rejectReason;
    
    /**
     * 好友备注名（仅同意时可设置）
     */
    private String remarkName;
    
    /**
     * 好友分组（仅同意时可设置）
     */
    private String friendGroup;
}