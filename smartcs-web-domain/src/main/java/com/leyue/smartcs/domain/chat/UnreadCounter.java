package com.leyue.smartcs.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读计数领域实体
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnreadCounter {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话标识（sessionId/groupId）
     */
    private String conversationId;
    
    /**
     * 未读计数
     */
    private Integer unreadCount;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 创建未读计数记录
     */
    public static UnreadCounter create(Long userId, String conversationId) {
        return UnreadCounter.builder()
                .userId(userId)
                .conversationId(conversationId)
                .unreadCount(0)
                .updatedAt(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 增加未读计数
     */
    public void increment() {
        this.unreadCount = (this.unreadCount == null ? 0 : this.unreadCount) + 1;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 增加指定数量的未读计数
     */
    public void incrementBy(int count) {
        this.unreadCount = (this.unreadCount == null ? 0 : this.unreadCount) + count;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 减少未读计数
     */
    public void decrement(int count) {
        this.unreadCount = Math.max(0, (this.unreadCount == null ? 0 : this.unreadCount) - count);
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 清零未读计数
     */
    public void reset() {
        this.unreadCount = 0;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 检查是否有未读消息
     */
    public boolean hasUnread() {
        return this.unreadCount != null && this.unreadCount > 0;
    }
}