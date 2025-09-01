package com.leyue.smartcs.chat;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.chat.convertor.MessageReactionConvertor;
import com.leyue.smartcs.chat.dataobject.CsMessageDO;
import com.leyue.smartcs.chat.dataobject.MessageReactionDO;
import com.leyue.smartcs.chat.mapper.CsMessageMapper;
import com.leyue.smartcs.chat.mapper.MessageReactionMapper;
import com.leyue.smartcs.domain.chat.MessageReaction;
import com.leyue.smartcs.domain.chat.ReactionSummary;
import com.leyue.smartcs.domain.chat.gateway.MessageReactionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息表情反应网关实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageReactionGatewayImpl implements MessageReactionGateway {
    
    private final MessageReactionMapper reactionMapper;
    private final CsMessageMapper messageMapper;
    private final MessageReactionConvertor convertor;

    @Override
    public MessageReaction addReaction(MessageReaction reaction) {
        try {
            MessageReactionDO dataObject = convertor.toDataObject(reaction);
            reactionMapper.insert(dataObject);
            
            // 更新消息的表情反应统计
            updateMessageReactionSummary(reaction.getMsgId());
            
            return convertor.toDomain(dataObject);
        } catch (Exception e) {
            log.error("添加表情反应失败: msgId={}, userId={}, emoji={}", 
                    reaction.getMsgId(), reaction.getUserId(), reaction.getReactionEmoji(), e);
            return null;
        }
    }
    
    @Override
    public boolean removeReaction(String msgId, String userId, String emoji) {
        try {
            int deleted = reactionMapper.deleteByMsgIdAndUserIdAndEmoji(msgId, userId, emoji);
            
            if (deleted > 0) {
                // 更新消息的表情反应统计
                updateMessageReactionSummary(msgId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("移除表情反应失败: msgId={}, userId={}, emoji={}", msgId, userId, emoji, e);
            return false;
        }
    }
    
    @Override
    public boolean toggleReaction(String msgId, String sessionId, String userId, String emoji, String name) {
        try {
            // 检查反应是否已存在
            MessageReactionDO existing = reactionMapper.selectByMsgIdAndUserIdAndEmoji(msgId, userId, emoji);
            
            if (existing != null) {
                // 存在则删除
                return removeReaction(msgId, userId, emoji);
            } else {
                // 不存在则添加
                MessageReaction newReaction = MessageReaction.create(msgId, sessionId, userId, emoji, name);
                MessageReaction added = addReaction(newReaction);
                return added != null;
            }
        } catch (Exception e) {
            log.error("切换表情反应失败: msgId={}, userId={}, emoji={}", msgId, userId, emoji, e);
            return false;
        }
    }
    
    @Override
    public List<MessageReaction> findByMsgId(String msgId) {
        try {
            List<MessageReactionDO> dataObjects = reactionMapper.selectByMsgId(msgId);
            return convertor.toDomainList(dataObjects);
        } catch (Exception e) {
            log.error("查询消息表情反应失败: msgId={}", msgId, e);
            return List.of();
        }
    }
    
    @Override
    public List<MessageReaction> findByMsgIdAndUserId(String msgId, String userId) {
        try {
            List<MessageReactionDO> dataObjects = reactionMapper.selectByMsgIdAndUserId(msgId, userId);
            return convertor.toDomainList(dataObjects);
        } catch (Exception e) {
            log.error("查询用户表情反应失败: msgId={}, userId={}", msgId, userId, e);
            return List.of();
        }
    }
    
    @Override
    public Optional<MessageReaction> findByMsgIdAndUserIdAndEmoji(String msgId, String userId, String emoji) {
        try {
            MessageReactionDO dataObject = reactionMapper.selectByMsgIdAndUserIdAndEmoji(msgId, userId, emoji);
            return dataObject != null ? Optional.of(convertor.toDomain(dataObject)) : Optional.empty();
        } catch (Exception e) {
            log.error("查询特定表情反应失败: msgId={}, userId={}, emoji={}", msgId, userId, emoji, e);
            return Optional.empty();
        }
    }
    
    @Override
    public int countReactionsByMsgId(String msgId) {
        try {
            return reactionMapper.countByMsgId(msgId);
        } catch (Exception e) {
            log.error("统计消息表情反应数量失败: msgId={}", msgId, e);
            return 0;
        }
    }
    
    @Override
    public int countReactionsByMsgIdAndEmoji(String msgId, String emoji) {
        try {
            return reactionMapper.countByMsgIdAndEmoji(msgId, emoji);
        } catch (Exception e) {
            log.error("统计特定表情反应数量失败: msgId={}, emoji={}", msgId, emoji, e);
            return 0;
        }
    }
    
    @Override
    public boolean deleteAllReactionsByMsgId(String msgId) {
        try {
            int deleted = reactionMapper.deleteByMsgId(msgId);
            log.info("删除消息所有表情反应: msgId={}, deleted={}", msgId, deleted);
            
            // 更新消息统计
            updateMessageReactionSummary(msgId);
            
            return true;
        } catch (Exception e) {
            log.error("删除消息所有表情反应失败: msgId={}", msgId, e);
            return false;
        }
    }
    
    @Override
    public boolean updateMessageReactionSummary(String msgId) {
        try {
            // 获取消息的所有反应
            List<MessageReaction> reactions = findByMsgId(msgId);
            
            // 生成统计摘要
            List<ReactionSummary> summaries = ReactionSummary.fromReactions(reactions, null);
            
            // 计算总数
            int totalCount = reactions.size();
            
            // 构建摘要JSON
            Map<String, Object> reactionsSummary = summaries.stream()
                    .collect(Collectors.toMap(
                            ReactionSummary::getEmoji,
                            summary -> Map.of(
                                    "name", summary.getName(),
                                    "count", summary.getCount(),
                                    "userIds", summary.getUserIds()
                            )
                    ));
            
            // 更新消息表的统计字段
            CsMessageDO updateMessage = new CsMessageDO();
            updateMessage.setReactionCount(totalCount);
            updateMessage.setReactionsSummary(reactionsSummary.isEmpty() ? null : JSON.toJSONString(reactionsSummary));
            
            LambdaQueryWrapper<CsMessageDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CsMessageDO::getMsgId, msgId);
            
            int updated = messageMapper.update(updateMessage, wrapper);
            log.debug("更新消息表情反应统计: msgId={}, totalCount={}, updated={}", msgId, totalCount, updated);
            
            return updated > 0;
        } catch (Exception e) {
            log.error("更新消息表情反应统计失败: msgId={}", msgId, e);
            return false;
        }
    }
}