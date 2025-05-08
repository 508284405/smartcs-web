package com.leyue.smartcs.chat.gatewayimpl;

import com.leyue.smartcs.chat.database.converter.SessionConverter;
import com.leyue.smartcs.chat.database.dataobject.CsSessionDO;
import com.leyue.smartcs.chat.database.mapper.CsSessionMapper;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.SessionState;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会话网关接口实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionGatewayImpl implements SessionGateway {
    
    private final CsSessionMapper sessionMapper;
    private final SessionConverter sessionConverter;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis缓存前缀
    private static final String CACHE_KEY_PREFIX = "session:";
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 缓存过期时间，1小时
    
    @Override
    public Long createSession(Session session) {
        CsSessionDO csSessionDO = sessionConverter.toDataObject(session);
        sessionMapper.insert(csSessionDO);
        return csSessionDO.getSessionId();
    }
    
    @Override
    public boolean updateSession(Session session) {
        CsSessionDO csSessionDO = sessionConverter.toDataObject(session);
        // 更新数据库
        boolean result = sessionMapper.updateById(csSessionDO) > 0;
        // 清除缓存
        if (result) {
            redisTemplate.delete(CACHE_KEY_PREFIX + csSessionDO.getSessionId());
        }
        return result;
    }
    
    @Override
    public Optional<Session> findById(Long sessionId) {
        // 先从Redis缓存中查询
        String cacheKey = CACHE_KEY_PREFIX + sessionId;
        Session cachedSession = (Session) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedSession != null) {
            return Optional.of(cachedSession);
        }
        
        // 从数据库中查询
        CsSessionDO csSessionDO = sessionMapper.selectById(sessionId);
        if (csSessionDO == null) {
            return Optional.empty();
        }
        
        // 转换为领域模型并缓存
        Session session = sessionConverter.toDomain(csSessionDO);
        redisTemplate.opsForValue().set(cacheKey, session, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        return Optional.of(session);
    }
    
    @Override
    public Optional<Session> findActiveSessionByCustomerId(Long customerId) {
        CsSessionDO csSessionDO = sessionMapper.findActiveSessionByCustomerId(customerId);
        return Optional.ofNullable(sessionConverter.toDomain(csSessionDO));
    }
    
    @Override
    public List<Session> findSessionsByCustomerId(Long customerId, int limit) {
        List<CsSessionDO> csSessionDOList = sessionMapper.findSessionsByCustomerId(customerId, limit);
        return csSessionDOList.stream()
                .map(sessionConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Session> findActiveSessionsByAgentId(Long agentId) {
        List<CsSessionDO> csSessionDOList = sessionMapper.findActiveSessionsByAgentId(agentId);
        return csSessionDOList.stream()
                .map(sessionConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean checkSessionExists(String sessionId) {
        // 先从Redis缓存中查询
        String cacheKey = CACHE_KEY_PREFIX + sessionId;
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        
        if (Boolean.TRUE.equals(hasKey)) {
            return true;
        }
        
        // 从数据库中查询
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        return sessionDO != null;
    }

    @Override
    public String getSessionStatus(String sessionId) {
        Session session = getSession(sessionId);
        if (session == null) {
            return null;
        }
        
        // 将SessionState枚举转换为字符串
        SessionState state = session.getSessionState();
        return state != null ? state.name() : null;
    }

    @Override
    public Session getSession(String sessionId) {
        // 先从Redis缓存中查询
        String cacheKey = CACHE_KEY_PREFIX + sessionId;
        Session cachedSession = (Session) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedSession != null) {
            return cachedSession;
        }
        
        // 从数据库中查询
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        if (sessionDO == null) {
            // 会话不存在
            return null;
        }
        
        // 转换为领域模型并缓存
        Session session = sessionConverter.toDomain(sessionDO);
        redisTemplate.opsForValue().set(cacheKey, session, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        return session;
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        // 从数据库中查询
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        if (sessionDO == null) {
            log.warn("会话不存在，无法更新状态: sessionId={}, status={}", sessionId, status);
            return;
        }
        
        // 状态转换：WAITING=0, ACTIVE=1, CLOSED=2
        int sessionState;
        switch (status) {
            case "WAITING":
                sessionState = 0;
                break;
            case "ACTIVE":
                sessionState = 1;
                break;
            case "CLOSED":
                sessionState = 2;
                break;
            default:
                log.warn("无效的会话状态: {}", status);
                return;
        }
        
        // 更新状态
        sessionDO.setSessionState(sessionState);
        sessionMapper.updateById(sessionDO);
        
        // 清除缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + sessionId);
        
        log.info("会话状态已更新: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void assignAgent(String sessionId, String agentId) {
        // 从数据库中查询
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        if (sessionDO == null) {
            log.warn("会话不存在，无法分配客服: sessionId={}, agentId={}", sessionId, agentId);
            return;
        }
        
        try {
            // 将String类型的agentId转换为Long类型
            Long agentIdLong = Long.parseLong(agentId);
            sessionDO.setAgentId(agentIdLong);
            
            // 状态设置为进行中(1)
            sessionDO.setSessionState(1);
            
            // 更新最后消息时间
            sessionDO.setLastMsgTime(LocalDateTime.now());
            
            // 更新数据库
            sessionMapper.updateById(sessionDO);
            
            // 清除缓存
            redisTemplate.delete(CACHE_KEY_PREFIX + sessionId);
            
            log.info("会话已分配客服: sessionId={}, agentId={}", sessionId, agentId);
        } catch (NumberFormatException e) {
            log.error("客服员ID转换失败: agentId={}", agentId, e);
        }
    }

    @Override
    public void closeSession(String sessionId, String reason) {
        // 从数据库中查询
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        if (sessionDO == null) {
            log.warn("会话不存在，无法关闭: sessionId={}, reason={}", sessionId, reason);
            return;
        }
        
        // 更新会话信息
        // 状态设置为已结束(2)
        sessionDO.setSessionState(2);
        
        // 更新最后消息时间
        sessionDO.setLastMsgTime(LocalDateTime.now());
        
        // 更新数据库
        sessionMapper.updateById(sessionDO);
        
        // 清除缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + sessionId);
        
        log.info("会话已关闭: sessionId={}, reason={}", sessionId, reason);
    }
}
