package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 未读计数数据对象，对应t_im_unread_counter表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_im_unread_counter", autoResultMap = true)
public class UnreadCounterDO extends BaseDO {
    
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
}