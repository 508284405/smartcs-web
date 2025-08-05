package com.leyue.smartcs.app.gateway;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.leyue.smartcs.app.convertor.AppTestSessionConvertor;
import com.leyue.smartcs.app.dataobject.AppTestSessionDO;
import com.leyue.smartcs.app.mapper.AppTestSessionMapper;
import com.leyue.smartcs.domain.app.entity.AppTestSession;
import com.leyue.smartcs.domain.app.gateway.AppTestSessionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI应用测试会话网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppTestSessionGatewayImpl implements AppTestSessionGateway {

    private final AppTestSessionMapper appTestSessionMapper;
    private final AppTestSessionConvertor appTestSessionConvertor;

    @Override
    public AppTestSession save(AppTestSession session) {
        try {
            AppTestSessionDO sessionDO = appTestSessionConvertor.toDataObject(session);
            
            if (session.getId() == null) {
                // 新增
                appTestSessionMapper.insert(sessionDO);
                session.setId(sessionDO.getId());
                log.info("创建应用测试会话: sessionId={}, appId={}", session.getSessionId(), session.getAppId());
            } else {
                // 更新
                appTestSessionMapper.updateById(sessionDO);
                log.info("更新应用测试会话: sessionId={}, appId={}", session.getSessionId(), session.getAppId());
            }
            
            return session;
        } catch (Exception e) {
            log.error("保存应用测试会话失败: sessionId={}, error={}", session.getSessionId(), e.getMessage(), e);
            throw new RuntimeException("保存应用测试会话失败", e);
        }
    }

    @Override
    public AppTestSession findBySessionId(String sessionId) {
        try {
            AppTestSessionDO sessionDO = appTestSessionMapper.selectBySessionId(sessionId);
            return sessionDO != null ? appTestSessionConvertor.toEntity(sessionDO) : null;
        } catch (Exception e) {
            log.error("根据会话ID查询会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<AppTestSession> findActiveSessionsByAppId(Long appId, Integer limit) {
        try {
            List<AppTestSessionDO> sessionDOs = appTestSessionMapper.findActiveSessionsByAppId(appId, limit);
            return sessionDOs.stream()
                    .map(appTestSessionConvertor::toEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据应用ID查询活跃会话失败: appId={}, error={}", appId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<AppTestSession> findSessionsByUserId(Long userId, Integer limit) {
        try {
            List<AppTestSessionDO> sessionDOs = appTestSessionMapper.findSessionsByUserId(userId, limit);
            return sessionDOs.stream()
                    .map(appTestSessionConvertor::toEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据用户ID查询会话失败: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public boolean updateSessionStats(String sessionId, Integer messageCount, Long lastMessageTime,
                                     Integer totalTokens, BigDecimal totalCost) {
        try {
            int result = appTestSessionMapper.updateSessionStats(sessionId, messageCount, lastMessageTime,
                    totalTokens, totalCost);
            log.info("更新会话统计: sessionId={}, messageCount={}, totalTokens={}, result={}",
                    sessionId, messageCount, totalTokens, result);
            return result > 0;
        } catch (Exception e) {
            log.error("更新会话统计失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateSessionState(String sessionId, String sessionState) {
        try {
            UpdateWrapper<AppTestSessionDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("session_id", sessionId)
                    .eq("is_deleted", 0)
                    .set("session_state", sessionState)
                    .set("updated_at", System.currentTimeMillis());
            
            if ("FINISHED".equals(sessionState)) {
                updateWrapper.set("end_time", System.currentTimeMillis());
            }
            
            int result = appTestSessionMapper.update(null, updateWrapper);
            log.info("更新会话状态: sessionId={}, sessionState={}, result={}", sessionId, sessionState, result);
            return result > 0;
        } catch (Exception e) {
            log.error("更新会话状态失败: sessionId={}, sessionState={}, error={}", sessionId, sessionState, e.getMessage(), e);
            return false;
        }
    }
}