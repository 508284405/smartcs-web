package com.leyue.smartcs.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 离线消息领域实体
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfflineMessage {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 接收者用户ID
     */
    private Long receiverId;
    
    /**
     * 会话标识（私聊用sessionId；群聊用groupId）
     */
    private String conversationId;
    
    /**
     * 消息ID（引用消息表）
     */
    private String msgId;
    
    /**
     * 消息摘要（用于快速列表展示）
     */
    private String msgBrief;
    
    /**
     * 入库时间
     */
    private Long createdAt;
    
    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
    
    /**
     * 创建离线消息
     */
    public static OfflineMessage create(Long receiverId, String conversationId, String msgId, String msgBrief) {
        return OfflineMessage.builder()
                .receiverId(receiverId)
                .conversationId(conversationId)
                .msgId(msgId)
                .msgBrief(msgBrief)
                .createdAt(System.currentTimeMillis())
                .isDeleted(0)
                .build();
    }
    
    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
    }
    
    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted == 1;
    }
}