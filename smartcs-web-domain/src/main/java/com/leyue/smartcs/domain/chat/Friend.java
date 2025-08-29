package com.leyue.smartcs.domain.chat;

import com.leyue.smartcs.domain.chat.enums.FriendStatus;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 好友关系领域模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    
    /**
     * 主键ID
     */
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
    
    /**
     * 获取好友状态枚举
     */
    public FriendStatus getFriendStatus() {
        return FriendStatus.fromCode(this.status);
    }
    
    /**
     * 检查是否为待审核状态
     */
    public boolean isPending() {
        return FriendStatus.PENDING.getCode().equals(this.status);
    }
    
    /**
     * 检查是否已成为好友
     */
    public boolean isAccepted() {
        return FriendStatus.ACCEPTED.getCode().equals(this.status);
    }
    
    /**
     * 检查是否被拒绝
     */
    public boolean isRejected() {
        return FriendStatus.REJECTED.getCode().equals(this.status);
    }
    
    /**
     * 检查是否被拉黑
     */
    public boolean isBlocked() {
        return FriendStatus.BLOCKED.getCode().equals(this.status);
    }
    
    /**
     * 同意好友申请
     */
    public void accept(String processedBy) {
        this.status = FriendStatus.ACCEPTED.getCode();
        this.processedAt = System.currentTimeMillis();
        this.processedBy = processedBy;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = processedBy;
    }
    
    /**
     * 拒绝好友申请
     */
    public void reject(String processedBy, String reason) {
        this.status = FriendStatus.REJECTED.getCode();
        this.processedAt = System.currentTimeMillis();
        this.processedBy = processedBy;
        this.rejectReason = reason;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = processedBy;
    }
    
    /**
     * 拉黑用户
     */
    public void block(String processedBy) {
        this.status = FriendStatus.BLOCKED.getCode();
        this.processedAt = System.currentTimeMillis();
        this.processedBy = processedBy;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = processedBy;
    }
    
    /**
     * 设置好友备注
     */
    public void setRemark(String remarkName, String updatedBy) {
        this.remarkName = remarkName;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 设置好友分组
     */
    public void setGroup(String friendGroup, String updatedBy) {
        this.friendGroup = friendGroup;
        this.updatedAt = System.currentTimeMillis();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return remarkName != null && !remarkName.trim().isEmpty() 
            ? remarkName : toUserId;
    }
    
    /**
     * 检查是否可以聊天
     */
    public boolean canChat() {
        return isAccepted() && !isBlocked();
    }
    
    /**
     * 检查申请是否过期（超过7天未处理）
     */
    public boolean isExpired() {
        if (!isPending() || appliedAt == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long expirationTime = 7 * 24 * 60 * 60 * 1000L; // 7天
        
        return (currentTime - appliedAt) > expirationTime;
    }
    
    /**
     * 创建好友申请
     */
    public static Friend createApplication(String fromUserId, String toUserId, 
                                         String applyMessage) {
        long currentTime = System.currentTimeMillis();
        
        return Friend.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(FriendStatus.PENDING.getCode())
                .applyMessage(applyMessage)
                .appliedAt(currentTime)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .createdBy(fromUserId)
                .updatedBy(fromUserId)
                .build();
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusText() {
        FriendStatus status = getFriendStatus();
        if (status != null) {
            return status.getDescription();
        }
        return "未知状态";
    }
    
    /**
     * 检查用户是否在此好友关系中
     */
    public boolean involveUser(String userId) {
        return fromUserId.equals(userId) || toUserId.equals(userId);
    }
    
    /**
     * 获取对方用户ID
     */
    public String getOtherUserId(String currentUserId) {
        if (fromUserId.equals(currentUserId)) {
            return toUserId;
        } else if (toUserId.equals(currentUserId)) {
            return fromUserId;
        }
        return null;
    }
}