package com.leyue.smartcs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.alibaba.fastjson2.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型上下文领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelContext {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 上下文消息列表（JSON格式）
     */
    private String messages;
    
    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;
    
    /**
     * 当前上下文长度
     */
    private Integer currentLength;
    
    /**
     * 逻辑删除标识
     */
    private Integer isDeleted;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 最后更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 上下文消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextMessage {
        /**
         * 消息角色（user, assistant, system）
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 消息时间戳
         */
        private Long timestamp;
        
        /**
         * 消息tokens数量（可选）
         */
        private Integer tokens;
    }
    
    /**
     * 验证上下文配置是否有效
     */
    public boolean isValid() {
        return sessionId != null && !sessionId.trim().isEmpty()
                && modelId != null;
    }
    
    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取消息列表
     */
    public List<ContextMessage> getMessagesList() {
        if (messages == null || messages.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseArray(messages, ContextMessage.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 设置消息列表
     */
    public void setMessagesList(List<ContextMessage> messagesList) {
        if (messagesList == null || messagesList.isEmpty()) {
            this.messages = "[]";
            this.currentLength = 0;
        } else {
            this.messages = JSON.toJSONString(messagesList);
            this.currentLength = calculateLength(messagesList);
        }
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 添加消息
     */
    public void addMessage(String role, String content) {
        List<ContextMessage> messagesList = getMessagesList();
        ContextMessage message = ContextMessage.builder()
                .role(role)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        
        messagesList.add(message);
        
        // 检查是否超出上下文窗口限制
        if (contextWindow != null && contextWindow > 0) {
            while (calculateLength(messagesList) > contextWindow && messagesList.size() > 1) {
                // 保留第一条消息（通常是系统消息），删除最早的用户/助手消息
                for (int i = 1; i < messagesList.size(); i++) {
                    if (!"system".equals(messagesList.get(i).getRole())) {
                        messagesList.remove(i);
                        break;
                    }
                }
            }
        }
        
        setMessagesList(messagesList);
    }
    
    /**
     * 添加用户消息
     */
    public void addUserMessage(String content) {
        addMessage("user", content);
    }
    
    /**
     * 添加助手消息
     */
    public void addAssistantMessage(String content) {
        addMessage("assistant", content);
    }
    
    /**
     * 添加系统消息
     */
    public void addSystemMessage(String content) {
        addMessage("system", content);
    }
    
    /**
     * 清空消息
     */
    public void clearMessages() {
        this.messages = "[]";
        this.currentLength = 0;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取最后一条消息
     */
    public ContextMessage getLastMessage() {
        List<ContextMessage> messagesList = getMessagesList();
        if (messagesList.isEmpty()) {
            return null;
        }
        return messagesList.get(messagesList.size() - 1);
    }
    
    /**
     * 获取用户消息
     */
    public List<ContextMessage> getUserMessages() {
        return getMessagesList().stream()
                .filter(msg -> "user".equals(msg.getRole()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取助手消息
     */
    public List<ContextMessage> getAssistantMessages() {
        return getMessagesList().stream()
                .filter(msg -> "assistant".equals(msg.getRole()))
                .collect(Collectors.toList());
    }
    
    /**
     * 是否为空上下文
     */
    public boolean isEmpty() {
        return getMessagesList().isEmpty();
    }
    
    /**
     * 是否接近上下文窗口限制
     */
    public boolean isNearLimit() {
        if (contextWindow == null || contextWindow <= 0) {
            return false;
        }
        return currentLength != null && currentLength > contextWindow * 0.8;
    }
    
    /**
     * 计算消息列表的长度（简单按字符数计算，实际应该按tokens计算）
     */
    private int calculateLength(List<ContextMessage> messagesList) {
        return messagesList.stream()
                .mapToInt(msg -> msg.getContent() != null ? msg.getContent().length() : 0)
                .sum();
    }
    
    /**
     * 获取对话轮数
     */
    public int getConversationRounds() {
        List<ContextMessage> userMessages = getUserMessages();
        return userMessages.size();
    }
}