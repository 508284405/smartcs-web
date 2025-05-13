package com.leyue.smartcs.domain.bot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息历史
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 最后更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 添加用户消息
     * @param content 消息内容
     */
    public void addUserMessage(String content) {
        Message message = Message.builder()
                .role("user")
                .content(content)
                .createdAt(System.currentTimeMillis())
                .build();
        messages.add(message);
        this.updatedAt = message.getCreatedAt();
    }
    
    /**
     * 添加助手消息
     * @param content 消息内容
     */
    public void addAssistantMessage(String content) {
        Message message = Message.builder()
                .role("assistant")
                .content(content)
                .createdAt(System.currentTimeMillis())
                .build();
        messages.add(message);
        this.updatedAt = message.getCreatedAt();
    }
    
    /**
     * 获取最近的N条消息
     * @param n 消息数量
     * @return 最近的消息列表
     */
    public List<Message> getRecentMessages(int n) {
        if (messages.size() <= n) {
            return new ArrayList<>(messages);
        }
        return messages.subList(messages.size() - n, messages.size());
    }
} 