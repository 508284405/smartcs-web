package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 好友关系数据对象
 */
@Data
@TableName(value = "cs_friend", autoResultMap = true)
public class FriendDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发起用户ID
     */
    private String fromUserId;
    
    /**
     * 目标用户ID
     */
    private String toUserId;
    
    /**
     * 好友状态 0-待审核 1-已同意 2-已拒绝 3-已拉黑
     */
    private Integer status;
    
    /**
     * 好友备注名
     */
    private String remarkName;
    
    /**
     * 好友分组
     */
    private String friendGroup;
    
    /**
     * 申请消息
     */
    private String applyMessage;
    
    /**
     * 申请时间
     */
    private Long appliedAt;
    
    /**
     * 处理时间
     */
    private Long processedAt;
    
    /**
     * 处理者用户ID
     */
    private String processedBy;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
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