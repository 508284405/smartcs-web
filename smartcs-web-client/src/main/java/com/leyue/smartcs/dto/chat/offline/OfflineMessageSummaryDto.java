package com.leyue.smartcs.dto.chat.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 离线消息摘要DTO
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfflineMessageSummaryDto {
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 未读消息数量
     */
    private Integer unreadCount;
    
    /**
     * 最后一条消息摘要
     */
    private String lastMessageBrief;
    
    /**
     * 最后一条消息时间
     */
    private Long lastMessageTime;
}