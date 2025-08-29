package com.leyue.smartcs.domain.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 会话设置领域模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSettings {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 是否置顶 0-否 1-是
     */
    private Integer isPinned;
    
    /**
     * 置顶时间
     */
    private Long pinnedAt;
    
    /**
     * 是否免打扰 0-否 1-是
     */
    private Integer isMuted;
    
    /**
     * 免打扰开始时间
     */
    private Long mutedAt;
    
    /**
     * 免打扰结束时间（永久免打扰为null）
     */
    private Long muteEndAt;
    
    /**
     * 是否归档 0-否 1-是
     */
    private Integer isArchived;
    
    /**
     * 归档时间
     */
    private Long archivedAt;
    
    /**
     * 自定义背景
     */
    private String customBackground;
    
    /**
     * 自定义通知声音
     */
    private String notificationSound;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
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
     * 检查是否置顶
     */
    public boolean isPinned() {
        return Integer.valueOf(1).equals(this.isPinned);
    }
    
    /**
     * 检查是否免打扰
     */
    public boolean isMuted() {
        if (!Integer.valueOf(1).equals(this.isMuted)) {
            return false;
        }
        
        // 检查是否过期
        if (muteEndAt != null && System.currentTimeMillis() > muteEndAt) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查是否归档
     */
    public boolean isArchived() {
        return Integer.valueOf(1).equals(this.isArchived);
    }
    
    /**
     * 置顶会话
     */
    public void pin(String updatedBy) {
        this.isPinned = 1;
        this.pinnedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 取消置顶
     */
    public void unpin(String updatedBy) {
        this.isPinned = 0;
        this.pinnedAt = null;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 设置免打扰
     */
    public void mute(String updatedBy, Long muteEndAt) {
        this.isMuted = 1;
        this.mutedAt = System.currentTimeMillis();
        this.muteEndAt = muteEndAt; // null表示永久免打扰
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 取消免打扰
     */
    public void unmute(String updatedBy) {
        this.isMuted = 0;
        this.mutedAt = null;
        this.muteEndAt = null;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 归档会话
     */
    public void archive(String updatedBy) {
        this.isArchived = 1;
        this.archivedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 取消归档
     */
    public void unarchive(String updatedBy) {
        this.isArchived = 0;
        this.archivedAt = null;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 设置自定义背景
     */
    public void setCustomBackground(String background, String updatedBy) {
        this.customBackground = background;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 设置通知声音
     */
    public void setNotificationSound(String sound, String updatedBy) {
        this.notificationSound = sound;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 获取免打扰剩余时间（毫秒）
     */
    public Long getMuteRemainingTime() {
        if (!isMuted() || muteEndAt == null) {
            return null;
        }
        
        long remaining = muteEndAt - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0L;
    }
    
    /**
     * 检查是否永久免打扰
     */
    public boolean isPermanentMute() {
        return isMuted() && muteEndAt == null;
    }
    
    /**
     * 创建默认设置
     */
    public static ConversationSettings createDefault(String userId, Long sessionId) {
        long currentTime = System.currentTimeMillis();
        
        return ConversationSettings.builder()
                .userId(userId)
                .sessionId(sessionId)
                .isPinned(0)
                .isMuted(0)
                .isArchived(0)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }
}