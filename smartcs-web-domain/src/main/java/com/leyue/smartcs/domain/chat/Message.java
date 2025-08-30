package com.leyue.smartcs.domain.chat;

import java.util.Date;

import com.leyue.smartcs.domain.chat.enums.MessageType;
import com.leyue.smartcs.domain.chat.enums.MessageSendStatus;

import lombok.Data;

/**
 * 消息领域模型
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    
    /**
     * 消息类型
     */
    private MessageType msgType;
    
    /**
     * 消息内容
     */
    private String content;

    /**
     * 聊天类型
     */
    private String chatType;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 时间戳
     */
    private Date timestamp;

    /**
     * 是否已撤回
     */
    private Boolean isRecalled;

    /**
     * 撤回时间戳
     */
    private Long recalledAt;

    /**
     * 撤回操作者ID
     */
    private String recalledBy;

    /**
     * 撤回原因
     */
    private String recallReason;

    /**
     * 是否被发送者删除
     */
    private Boolean isDeletedBySender;

    /**
     * 是否被接收者删除
     */
    private Boolean isDeletedByReceiver;

    /**
     * 发送者删除时间戳
     */
    private Long deletedBySenderAt;

    /**
     * 接收者删除时间戳
     */
    private Long deletedByReceiverAt;

    /**
     * 删除类型 0-仅自己可见删除 1-双方删除
     */
    private Integer deleteType;

    /**
     * 删除原因
     */
    private String deletedReason;

    /**
     * 是否已编辑
     */
    private Boolean isEdited;

    /**
     * 编辑时间戳
     */
    private Long editedAt;

    /**
     * 原始内容（用于编辑历史）
     */
    private String originalContent;

    /**
     * 编辑次数
     */
    private Integer editCount;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 读取时间戳
     */
    private Long readAt;

    /**
     * 读取者ID
     */
    private String readBy;

    /**
     * 消息发送状态 0-发送中 1-已送达 2-发送失败 3-已读
     */
    private Integer sendStatus;

    /**
     * 发送失败原因
     */
    private String sendFailReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 回复的消息ID
     */
    private String replyToMsgId;

    /**
     * 被回复的消息内容（用于显示引用）
     */
    private String quotedContent;

    /**
     * 被回复的消息发送者
     */
    private String quotedFromUser;

    
    /**
     * 检查是否为文本消息
     */
    public boolean isTextMessage() {
        return MessageType.TEXT.equals(this.msgType);
    }
    
    /**
     * 检查是否为图片消息
     */
    public boolean isImageMessage() {
        return MessageType.IMAGE.equals(this.msgType);
    }

    /**
     * 检查是否已撤回
     */
    public boolean isRecalled() {
        return Boolean.TRUE.equals(this.isRecalled);
    }

    /**
     * 检查是否可以撤回
     * 规则：发送后2分钟内且未被撤回
     */
    public boolean canRecall(String userId) {
        if (isRecalled()) {
            return false;
        }
        
        // 检查是否是发送者本人
        if (!userId.equals(this.chatType) && !"USER".equals(this.chatType)) {
            return false;
        }
        
        // 检查时间限制 (2分钟 = 120,000毫秒)
        long currentTime = System.currentTimeMillis();
        long messageTime = this.createdAt != null ? this.createdAt : 
                          (this.timestamp != null ? this.timestamp.getTime() : currentTime);
        
        return (currentTime - messageTime) <= 120_000;
    }

    /**
     * 检查是否被指定用户删除
     */
    public boolean isDeletedBy(String userId) {
        // 这里需要根据实际的用户角色判断逻辑来实现
        // 暂时简化处理
        return Boolean.TRUE.equals(this.isDeletedBySender) || Boolean.TRUE.equals(this.isDeletedByReceiver);
    }

    /**
     * 检查是否可以删除
     * 规则：发送者可以删除自己的消息，接收者可以删除收到的消息
     */
    public boolean canDelete(String userId) {
        if (isRecalled()) {
            return false; // 已撤回的消息不能再删除
        }
        
        // 已经被当前用户删除的消息不能再次删除
        if (isDeletedBy(userId)) {
            return false;
        }
        
        return true; // 其他情况都允许删除
    }

    /**
     * 执行删除操作
     */
    public void deleteBy(String userId, Integer deleteType, String reason) {
        long currentTime = System.currentTimeMillis();
        
        // 根据用户角色设置不同的删除状态
        // 这里简化处理，实际应该根据用户在会话中的角色判断
        if ("USER".equals(this.chatType)) {
            // 用户消息，只有发送者可以删除
            this.isDeletedBySender = true;
            this.deletedBySenderAt = currentTime;
        } else {
            // 其他类型消息，标记为接收者删除
            this.isDeletedByReceiver = true;
            this.deletedByReceiverAt = currentTime;
        }
        
        this.deleteType = deleteType;
        this.deletedReason = reason;
    }

    /**
     * 检查是否已编辑
     */
    public boolean isEdited() {
        return Boolean.TRUE.equals(this.isEdited);
    }

    /**
     * 检查是否可以编辑
     * 规则：只有文本消息可以编辑，且未撤回、未删除，发送后30分钟内
     */
    public boolean canEdit(String userId) {
        // 只有文本消息可以编辑
        if (!isTextMessage()) {
            return false;
        }
        
        // 已撤回的消息不能编辑
        if (isRecalled()) {
            return false;
        }
        
        // 已删除的消息不能编辑
        if (isDeletedBy(userId)) {
            return false;
        }
        
        // 检查是否是发送者本人（这里需要根据实际的用户角色判断逻辑）
        // 暂时简化处理
        
        // 检查时间限制（30分钟 = 1,800,000毫秒）
        long currentTime = System.currentTimeMillis();
        long messageTime = this.createdAt != null ? this.createdAt : 
                          (this.timestamp != null ? this.timestamp.getTime() : currentTime);
        
        return (currentTime - messageTime) <= 1_800_000;
    }

    /**
     * 执行编辑操作
     */
    public void editContent(String newContent) {
        if (this.originalContent == null) {
            // 首次编辑，保存原始内容
            this.originalContent = this.content;
        }
        
        // 更新内容
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = System.currentTimeMillis();
        
        // 增加编辑次数
        if (this.editCount == null) {
            this.editCount = 1;
        } else {
            this.editCount++;
        }
    }

    /**
     * 获取编辑历史标识
     */
    public String getEditHistoryText() {
        if (!isEdited()) {
            return null;
        }
        
        return String.format("已编辑 %d 次", this.editCount != null ? this.editCount : 0);
    }

    /**
     * 检查是否已读
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(this.isRead);
    }

    /**
     * 标记为已读
     */
    public void markAsRead(String userId) {
        this.isRead = true;
        this.readAt = System.currentTimeMillis();
        this.readBy = userId;
        this.sendStatus = MessageSendStatus.READ.getCode();
    }

    /**
     * 检查是否可以标记为已读
     * 规则：消息发送者不能标记自己的消息为已读
     */
    public boolean canMarkAsRead(String userId) {
        // 简化处理，实际应该根据业务逻辑判断用户是否为消息发送者
        // 已读的消息不需要重复标记
        if (isRead()) {
            return false;
        }
        
        // 已撤回或删除的消息通常不需要标记已读
        if (isRecalled() || isDeletedBy(userId)) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取已读状态文本
     */
    public String getReadStatusText() {
        if (!isRead()) {
            return "未读";
        }
        
        return "已读";
    }

    /**
     * 获取消息发送状态
     */
    public MessageSendStatus getSendStatus() {
        return MessageSendStatus.fromCode(this.sendStatus);
    }

    /**
     * 检查是否正在发送
     */
    public boolean isSending() {
        return MessageSendStatus.SENDING.getCode().equals(this.sendStatus);
    }

    /**
     * 检查是否已送达
     */
    public boolean isDelivered() {
        return MessageSendStatus.DELIVERED.getCode().equals(this.sendStatus);
    }

    /**
     * 检查发送是否失败
     */
    public boolean isSendFailed() {
        return MessageSendStatus.SEND_FAILED.getCode().equals(this.sendStatus);
    }

    /**
     * 标记为发送中
     */
    public void markAsSending() {
        this.sendStatus = MessageSendStatus.SENDING.getCode();
        this.sendFailReason = null;
    }

    /**
     * 标记为已送达
     */
    public void markAsDelivered() {
        this.sendStatus = MessageSendStatus.DELIVERED.getCode();
        this.sendFailReason = null;
    }

    /**
     * 标记为发送失败
     */
    public void markAsSendFailed(String reason) {
        this.sendStatus = MessageSendStatus.SEND_FAILED.getCode();
        this.sendFailReason = reason;
    }


    /**
     * 检查是否可以重试发送
     */
    public boolean canRetry() {
        if (!isSendFailed()) {
            return false;
        }
        
        // 最大重试3次
        int maxRetries = 3;
        int currentRetries = this.retryCount != null ? this.retryCount : 0;
        
        return currentRetries < maxRetries;
    }

    /**
     * 增加重试次数并标记为发送中
     */
    public void retry() {
        if (!canRetry()) {
            throw new IllegalStateException("消息无法重试：可能不是失败状态或已达最大重试次数");
        }
        
        if (this.retryCount == null) {
            this.retryCount = 1;
        } else {
            this.retryCount++;
        }
        
        markAsSending();
    }

    /**
     * 获取发送状态描述文本
     */
    public String getSendStatusText() {
        MessageSendStatus status = getSendStatus();
        if (status == null) {
            return "未知状态";
        }
        
        switch (status) {
            case SENDING:
                return "发送中...";
            case DELIVERED:
                return "已送达";
            case SEND_FAILED:
                return "发送失败";
            case READ:
                return "已读";
            default:
                return status.getDescription();
        }
    }

    /**
     * 获取发送状态图标
     */
    public String getSendStatusIcon() {
        MessageSendStatus status = getSendStatus();
        if (status == null) {
            return "❓";
        }
        
        switch (status) {
            case SENDING:
                return "⏳";
            case DELIVERED:
                return "✓";
            case SEND_FAILED:
                return "❌";
            case READ:
                return "✓✓";
            default:
                return "❓";
        }
    }

    /**
     * 检查是否为回复消息
     */
    public boolean isReplyMessage() {
        return this.replyToMsgId != null && !this.replyToMsgId.trim().isEmpty();
    }

    /**
     * 设置回复信息
     */
    public void setReplyInfo(String replyToMsgId, String quotedContent, String quotedFromUser) {
        this.replyToMsgId = replyToMsgId;
        this.quotedContent = quotedContent;
        this.quotedFromUser = quotedFromUser;
    }

    /**
     * 清除回复信息
     */
    public void clearReplyInfo() {
        this.replyToMsgId = null;
        this.quotedContent = null;
        this.quotedFromUser = null;
    }

    /**
     * 获取引用内容的摘要（用于显示）
     */
    public String getQuotedSummary() {
        if (!isReplyMessage() || this.quotedContent == null) {
            return null;
        }
        
        // 限制引用内容长度，超过50个字符显示省略号
        if (this.quotedContent.length() <= 50) {
            return this.quotedContent;
        }
        
        return this.quotedContent.substring(0, 47) + "...";
    }

    /**
     * 获取完整的引用显示文本
     */
    public String getQuoteDisplayText() {
        if (!isReplyMessage()) {
            return null;
        }
        
        String fromUser = this.quotedFromUser != null ? this.quotedFromUser : "未知用户";
        String summary = getQuotedSummary();
        
        return String.format("回复 %s: %s", fromUser, summary);
    }

    /**
     * 检查是否可以被回复
     * 规则：只有未撤回、未删除的消息可以被回复
     */
    public boolean canBeReplied() {
        // 已撤回的消息不能被回复
        if (isRecalled()) {
            return false;
        }
        
        // 已删除的消息不能被回复
        if (Boolean.TRUE.equals(this.isDeletedBySender) || 
            Boolean.TRUE.equals(this.isDeletedByReceiver)) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取回复链深度（避免过深的回复嵌套）
     */
    public int getReplyDepth() {
        if (!isReplyMessage()) {
            return 0;
        }
        
        // 这里简化处理，实际应该递归查找整个回复链
        // 返回1表示这是一级回复
        return 1;
    }
}
