package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.api.UnreadCounterService;
import com.leyue.smartcs.domain.chat.gateway.UnreadCounterGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 未读计数服务实现
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnreadCounterServiceImpl implements UnreadCounterService {

    private final UnreadCounterGateway unreadCounterGateway;

    @Override
    public int incrementUnreadCount(Long userId, String conversationId, int increment) {
        log.debug("增加未读计数: userId={}, conversationId={}, increment={}", userId, conversationId, increment);
        
        if (increment <= 0) {
            return getUnreadCount(userId, conversationId);
        }
        
        return unreadCounterGateway.incrementUnreadCount(userId, conversationId, increment);
    }

    @Override
    public int decrementUnreadCount(Long userId, String conversationId, int decrement) {
        log.debug("减少未读计数: userId={}, conversationId={}, decrement={}", userId, conversationId, decrement);
        
        if (decrement <= 0) {
            return getUnreadCount(userId, conversationId);
        }
        
        return unreadCounterGateway.decrementUnreadCount(userId, conversationId, decrement);
    }

    @Override
    public boolean resetUnreadCount(Long userId, String conversationId) {
        log.debug("重置未读计数: userId={}, conversationId={}", userId, conversationId);
        
        return unreadCounterGateway.resetUnreadCount(userId, conversationId);
    }

    @Override
    public int getUnreadCount(Long userId, String conversationId) {
        return unreadCounterGateway.getUnreadCount(userId, conversationId);
    }

    @Override
    public Map<String, Integer> getAllUnreadCounts(Long userId) {
        log.debug("获取用户所有未读计数: userId={}", userId);
        
        return unreadCounterGateway.getAllUnreadCounts(userId);
    }
}