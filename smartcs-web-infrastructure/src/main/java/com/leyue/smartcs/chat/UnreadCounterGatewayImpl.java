package com.leyue.smartcs.chat;

import com.leyue.smartcs.chat.convertor.UnreadCounterConvertor;
import com.leyue.smartcs.chat.dataobject.UnreadCounterDO;
import com.leyue.smartcs.chat.mapper.UnreadCounterMapper;
import com.leyue.smartcs.domain.chat.UnreadCounter;
import com.leyue.smartcs.domain.chat.gateway.UnreadCounterGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 未读计数网关接口实现
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnreadCounterGatewayImpl implements UnreadCounterGateway {
    
    private final UnreadCounterMapper unreadCounterMapper;
    private final UnreadCounterConvertor unreadCounterConvertor;
    
    @Override
    public Long saveOrUpdate(UnreadCounter unreadCounter) {
        log.debug("保存或更新未读计数: {}", unreadCounter);
        UnreadCounterDO unreadCounterDO = unreadCounterConvertor.toDataObject(unreadCounter);
        
        int affected = unreadCounterMapper.saveOrUpdate(unreadCounterDO);
        if (affected > 0) {
            return unreadCounterDO.getId();
        }
        return null;
    }
    
    @Override
    public int incrementUnreadCount(Long userId, String conversationId, int increment) {
        log.debug("增加未读计数: userId={}, conversationId={}, increment={}", userId, conversationId, increment);
        
        // 首先尝试更新现有记录
        int updatedRows = unreadCounterMapper.incrementUnreadCount(userId, conversationId, increment);
        
        // 如果没有更新任何行，说明记录不存在，需要创建新记录
        if (updatedRows == 0) {
            UnreadCounter newCounter = UnreadCounter.create(userId, conversationId);
            newCounter.incrementBy(increment);
            saveOrUpdate(newCounter);
            return increment;
        }
        
        // 返回更新后的计数（这里需要查询获取）
        return getUnreadCount(userId, conversationId);
    }
    
    @Override
    public int decrementUnreadCount(Long userId, String conversationId, int decrement) {
        log.debug("减少未读计数: userId={}, conversationId={}, decrement={}", userId, conversationId, decrement);
        int updatedRows = unreadCounterMapper.decrementUnreadCount(userId, conversationId, decrement);
        
        if (updatedRows > 0) {
            return getUnreadCount(userId, conversationId);
        }
        return 0;
    }
    
    @Override
    public boolean resetUnreadCount(Long userId, String conversationId) {
        log.debug("重置未读计数: userId={}, conversationId={}", userId, conversationId);
        int updatedRows = unreadCounterMapper.resetUnreadCount(userId, conversationId);
        return updatedRows > 0;
    }
    
    @Override
    public int getUnreadCount(Long userId, String conversationId) {
        UnreadCounterDO unreadCounterDO = unreadCounterMapper.findByUserAndConversation(userId, conversationId);
        if (unreadCounterDO != null) {
            return unreadCounterDO.getUnreadCount() != null ? unreadCounterDO.getUnreadCount() : 0;
        }
        return 0;
    }
    
    @Override
    public Map<String, Integer> getAllUnreadCounts(Long userId) {
        log.debug("获取用户所有未读计数: userId={}", userId);
        List<UnreadCounterDO> unreadCounterDOs = unreadCounterMapper.findAllByUser(userId);
        
        return unreadCounterDOs.stream()
                .collect(Collectors.toMap(
                        UnreadCounterDO::getConversationId,
                        counter -> counter.getUnreadCount() != null ? counter.getUnreadCount() : 0
                ));
    }
    
    @Override
    public List<UnreadCounter> findByUserAndConversations(Long userId, List<String> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量获取未读计数: userId={}, conversationIds={}", userId, conversationIds);
        List<UnreadCounterDO> unreadCounterDOs = unreadCounterMapper.findByUserAndConversations(userId, conversationIds);
        return unreadCounterConvertor.toDomains(unreadCounterDOs);
    }
    
    @Override
    public boolean deleteUnreadCounter(Long userId, String conversationId) {
        log.debug("删除未读计数: userId={}, conversationId={}", userId, conversationId);
        int deletedRows = unreadCounterMapper.deleteByUserAndConversation(userId, conversationId);
        return deletedRows > 0;
    }
}