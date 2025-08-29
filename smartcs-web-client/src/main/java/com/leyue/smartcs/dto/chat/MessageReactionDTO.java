package com.leyue.smartcs.dto.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 消息表情反应DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageReactionDTO {
    
    /**
     * 反应ID
     */
    private Long id;
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
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
     * 添加时间戳
     */
    private Long createdAt;
}