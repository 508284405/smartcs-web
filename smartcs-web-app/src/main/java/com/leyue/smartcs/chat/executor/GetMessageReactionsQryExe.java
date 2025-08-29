package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.chat.MessageReaction;
import com.leyue.smartcs.domain.chat.ReactionSummary;
import com.leyue.smartcs.domain.chat.gateway.MessageReactionGateway;
import com.leyue.smartcs.dto.chat.ReactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询消息表情反应执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetMessageReactionsQryExe {
    
    private final MessageReactionGateway reactionGateway;
    
    /**
     * 获取消息的表情反应统计
     */
    public SingleResponse<List<ReactionDTO>> execute(String msgId) {
        try {
            // 获取当前用户ID
            String currentUserId = UserContext.getCurrentUserId();
            
            // 查询消息的所有反应
            List<MessageReaction> reactions = reactionGateway.findByMsgId(msgId);
            
            // 生成统计摘要
            List<ReactionSummary> summaries = ReactionSummary.fromReactions(reactions, currentUserId);
            
            // 转换为DTO
            List<ReactionDTO> reactionDTOs = summaries.stream()
                    .map(summary -> ReactionDTO.builder()
                            .emoji(summary.getEmoji())
                            .name(summary.getName())
                            .count(summary.getCount())
                            .userIds(summary.getUserIds())
                            .hasReacted(summary.getHasReacted())
                            .build())
                    .collect(Collectors.toList());
            
            return SingleResponse.of(reactionDTOs);
            
        } catch (Exception e) {
            log.error("查询消息表情反应失败: msgId={}", msgId, e);
            return SingleResponse.buildFailure("查询消息表情反应失败: " + e.getMessage());
        }
    }
}