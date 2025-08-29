package com.leyue.smartcs.domain.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 消息表情反应领域模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageReaction {
    
    /**
     * 主键ID
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
    private String reactionEmoji;
    
    /**
     * 表情名称
     */
    private String reactionName;
    
    /**
     * 添加时间戳
     */
    private Long createdAt;
    
    /**
     * 更新时间戳
     */
    private Long updatedAt;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
     */
    private String updatedBy;
    
    /**
     * 验证表情反应是否有效
     */
    public boolean isValid() {
        return msgId != null && !msgId.trim().isEmpty()
                && userId != null && !userId.trim().isEmpty()
                && reactionEmoji != null && !reactionEmoji.trim().isEmpty()
                && reactionName != null && !reactionName.trim().isEmpty();
    }
    
    /**
     * 判断是否为同一个用户的反应
     */
    public boolean isFromUser(String targetUserId) {
        return userId != null && userId.equals(targetUserId);
    }
    
    /**
     * 判断是否为相同的表情反应
     */
    public boolean isSameReaction(String emoji) {
        return reactionEmoji != null && reactionEmoji.equals(emoji);
    }
    
    /**
     * 创建表情反应
     */
    public static MessageReaction create(String msgId, String sessionId, String userId, 
                                       String emoji, String name) {
        return MessageReaction.builder()
                .msgId(msgId)
                .sessionId(sessionId)
                .userId(userId)
                .reactionEmoji(emoji)
                .reactionName(name)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }
}