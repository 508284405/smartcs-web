package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表情反应WebSocket消息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionMessage {
    
    /**
     * 消息类型
     */
    private String type = "REACTION_UPDATE";
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 操作类型 (add/remove/update)
     */
    private String action;
    
    /**
     * 操作用户ID
     */
    private String userId;
    
    /**
     * 表情符号
     */
    private String emoji;
    
    /**
     * 表情名称
     */
    private String name;
    
    /**
     * 操作时间戳
     */
    private Long timestamp;
    
    /**
     * 更新后的完整反应统计
     */
    private List<ReactionSummary> reactions;
    
    /**
     * 反应统计摘要
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReactionSummary {
        /**
         * 表情符号
         */
        private String emoji;
        
        /**
         * 表情名称
         */
        private String name;
        
        /**
         * 反应总数
         */
        private Integer count;
        
        /**
         * 反应用户列表
         */
        private List<String> userIds;
    }
}