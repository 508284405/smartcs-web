package com.leyue.smartcs.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bot上下文DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotContextDTO {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 历史消息列表
     */
    private List<Message> history;
    
    /**
     * 总消息数
     */
    private Integer totalMessages;
    
    /**
     * 最后更新时间（毫秒时间戳）
     */
    private Long lastUpdatedAt;
    
    /**
     * 消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 消息ID
         */
        private String id;
        
        /**
         * 角色（user/assistant）
         */
        private String role;
        
        /**
         * 内容
         */
        private String content;
        
        /**
         * 创建时间（毫秒时间戳）
         */
        private Long createdAt;
    }
} 