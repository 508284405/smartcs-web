package com.leyue.smartcs.dto.chat.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 离线消息详情DTO
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfflineMessagesDto {
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 离线消息列表
     */
    private List<OfflineMessageDto> messages;
    
    /**
     * 是否还有更多消息
     */
    private Boolean hasMore;
    
    /**
     * 单条离线消息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OfflineMessageDto {
        
        /**
         * 消息ID
         */
        private String msgId;
        
        /**
         * 消息摘要
         */
        private String msgBrief;
        
        /**
         * 消息时间
         */
        private Long createdAt;
    }
}