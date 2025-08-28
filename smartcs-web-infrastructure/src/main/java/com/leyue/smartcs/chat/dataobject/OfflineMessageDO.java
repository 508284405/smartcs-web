package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 离线消息数据对象，对应t_im_offline_message表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_im_offline_message", autoResultMap = true)
public class OfflineMessageDO extends BaseDO {
    
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
}