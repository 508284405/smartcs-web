package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会话设置数据对象
 */
@Data
@TableName(value = "cs_conversation_settings", autoResultMap = true)
public class ConversationSettingsDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 免打扰结束时间
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
}