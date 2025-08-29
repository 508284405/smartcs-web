package com.leyue.smartcs.domain.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息表情反应统计
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionSummary {
    
    /**
     * 表情符号
     */
    private String emoji;
    
    /**
     * 表情名称
     */
    private String name;
    
    /**
     * 反应总数
     */
    private Integer count;
    
    /**
     * 反应用户列表
     */
    private List<String> userIds;
    
    /**
     * 当前用户是否已反应
     */
    private Boolean hasReacted;
    
    /**
     * 从反应列表创建统计摘要
     */
    public static List<ReactionSummary> fromReactions(List<MessageReaction> reactions, String currentUserId) {
        if (reactions == null || reactions.isEmpty()) {
            return List.of();
        }
        
        // 按表情符号分组统计
        Map<String, List<MessageReaction>> reactionGroups = reactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getReactionEmoji));
        
        return reactionGroups.entrySet().stream()
                .map(entry -> {
                    String emoji = entry.getKey();
                    List<MessageReaction> reactionList = entry.getValue();
                    
                    // 获取表情名称（取第一个的名称）
                    String name = reactionList.get(0).getReactionName();
                    
                    // 统计数量和用户列表
                    List<String> userIds = reactionList.stream()
                            .map(MessageReaction::getUserId)
                            .distinct()
                            .collect(Collectors.toList());
                    
                    // 检查当前用户是否已反应
                    boolean hasReacted = currentUserId != null && 
                            userIds.contains(currentUserId);
                    
                    return ReactionSummary.builder()
                            .emoji(emoji)
                            .name(name)
                            .count(userIds.size())
                            .userIds(userIds)
                            .hasReacted(hasReacted)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否有效的反应统计
     */
    public boolean isValid() {
        return emoji != null && !emoji.trim().isEmpty() 
                && count != null && count > 0
                && userIds != null && !userIds.isEmpty();
    }
}