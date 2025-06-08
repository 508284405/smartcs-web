package com.leyue.smartcs.chat;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.chat.convertor.SessionConverter;
import com.leyue.smartcs.chat.dataobject.CsSessionDO;
import com.leyue.smartcs.chat.mapper.CsSessionMapper;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.enums.SessionState;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.dto.chat.SessionPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

    @Override
    public Long createSession(Session session) {
        CsSessionDO csSessionDO = sessionConverter.toDataObject(session);
        sessionMapper.insert(csSessionDO);
        return csSessionDO.getSessionId();
    }

    @Override
    @CacheEvict(value = "sessions", key = "#session.sessionId")
    public boolean updateSession(Session session) {
        CsSessionDO csSessionDO = sessionConverter.toDataObject(session);
        boolean result = false;
        if (session.getId() != null) {
            result = sessionMapper.updateById(csSessionDO) > 0;
        } else if (session.getSessionId() != null) {
            result = sessionMapper.update(csSessionDO, new LambdaQueryWrapper<CsSessionDO>()
                    .eq(CsSessionDO::getSessionId, csSessionDO.getSessionId())) > 0;
        }

        return result;
    }

    @Override
    @Cacheable(value = "sessions", key = "#sessionId")
    public Optional<Session> findBySessionId(Long sessionId) {
        CsSessionDO csSessionDO = sessionMapper.selectBySessionId(sessionId);
        if (csSessionDO == null) {
            return Optional.empty();
        }

        Session session = sessionConverter.toDomain(csSessionDO);
        return Optional.of(session);
    }

    @Override
    public Optional<Session> findActiveSessionByCustomerId(Long customerId) {
        CsSessionDO csSessionDO = sessionMapper.findActiveSessionByCustomerId(customerId);
        return Optional.ofNullable(sessionConverter.toDomain(csSessionDO));
    }

    @Override
    public Session findCustomerActiveSession(Long customerId) {
        CsSessionDO csSessionDO = sessionMapper.findCustomerActiveSession(customerId);
        return csSessionDO != null ? sessionConverter.toDomain(csSessionDO) : null;
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
    public boolean checkSessionExists(Long sessionId) {
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        return sessionDO != null;
    }

    @Override
    @Cacheable(value = "sessionStatus", key = "#sessionId")
    public String getSessionStatus(Long sessionId) {
        Session session = getSession(sessionId);
        if (session == null) {
            return null;
        }

        // 将SessionState枚举转换为字符串
        SessionState state = session.getSessionState();
        return state != null ? state.name() : null;
    }

    @Override
    @Cacheable(value = "sessions", key = "#sessionId")
    public Session getSession(Long sessionId) {
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        if (sessionDO == null) {
            // 会话不存在
            return null;
        }

        // 转换为领域模型
        Session session = sessionConverter.toDomain(sessionDO);
        return session;
    }

    @Override
    @CacheEvict(value = {"sessions", "sessionStatus"}, key = "#sessionId")
    public void updateSessionStatus(Long sessionId, String status) {
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

        log.info("会话状态已更新: sessionId={}, status={}", sessionId, status);
    }

    @Override
    @CacheEvict(value = {"sessions", "sessionStatus"}, key = "#sessionId")
    public void assignAgent(Long sessionId, String agentId) {
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
            sessionDO.setLastMsgTime(System.currentTimeMillis());

            // 更新数据库
            sessionMapper.updateById(sessionDO);

            log.info("会话已分配客服: sessionId={}, agentId={}", sessionId, agentId);
        } catch (NumberFormatException e) {
            log.error("客服员ID转换失败: agentId={}", agentId, e);
        }
    }

    @Override
    @CacheEvict(value = {"sessions", "sessionStatus"}, key = "#sessionId")
    public void closeSession(Long sessionId, String reason) {
        CsSessionDO sessionDO = sessionMapper.selectBySessionId(sessionId);
        if (sessionDO == null) {
            log.warn("会话不存在，无法关闭: sessionId={}, reason={}", sessionId, reason);
            return;
        }

        // 更新会话信息
        // 状态设置为已结束(2)
        sessionDO.setSessionState(2);

        // 更新最后消息时间
        sessionDO.setLastMsgTime(System.currentTimeMillis());

        // 更新数据库
        sessionMapper.updateById(sessionDO);

        log.info("会话已关闭: sessionId={}, reason={}", sessionId, reason);
    }

    @Override
    public PageResponse<Session> pageSessions(SessionPageQuery query) {
        LambdaQueryWrapper<CsSessionDO> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (query.getSessionId() != null) {
            wrapper.eq(CsSessionDO::getSessionId, query.getSessionId());
        }

        if (query.getCustomerId() != null) {
            wrapper.eq(CsSessionDO::getCustomerId, query.getCustomerId());
        }

        if (query.getAgentId() != null) {
            wrapper.eq(CsSessionDO::getAgentId, query.getAgentId());
        }

        if (StringUtils.isNotBlank(query.getStatus())) {
            int statusCode;
            switch (query.getStatus()) {
                case "WAITING":
                    statusCode = 0;
                    break;
                case "ACTIVE":
                    statusCode = 1;
                    break;
                case "CLOSED":
                    statusCode = 2;
                    break;
                default:
                    log.warn("无效的会话状态过滤条件: {}", query.getStatus());
                    statusCode = -1;
            }

            if (statusCode >= 0) {
                wrapper.eq(CsSessionDO::getSessionState, statusCode);
            }
        }

        // 按创建时间倒序排序
        wrapper.orderByDesc(CsSessionDO::getCreatedAt);

        // 执行分页查询
        Page<CsSessionDO> page = new Page<>(query.getPageIndex(), query.getPageSize());
        IPage<CsSessionDO> result = sessionMapper.selectPage(page, wrapper);

        // 转换为领域模型
        List<Session> sessions = result.getRecords().stream()
                .map(sessionConverter::toDomain)
                .collect(Collectors.toList());

        // 返回分页结果
        return PageResponse.of(sessions, (int) result.getTotal(), query.getPageSize(), query.getPageIndex());
    }

    @Override
    public Optional<Session> getWaitingSession(Long customerId) {
        CsSessionDO sessionDO = sessionMapper.selectOne(new LambdaQueryWrapper<CsSessionDO>()
                .eq(CsSessionDO::getCustomerId, customerId).eq(CsSessionDO::getSessionState, 0));
        return Optional.ofNullable(sessionConverter.toDomain(sessionDO));
    }

    @Override
    @CacheEvict(value = {"sessions", "sessionStatus"}, key = "#sessionId")
    public void updateSessionStatus(Long sessionId, SessionState sessionState) {
        sessionMapper.update(new LambdaUpdateWrapper<CsSessionDO>()
                .eq(CsSessionDO::getSessionId, sessionId)
                .set(CsSessionDO::getSessionState, sessionState.getCode()));
    }

    @Override
    public void updateSessionAgent(Long sessionId, Long targetBotId) {
        sessionMapper.update(new LambdaUpdateWrapper<CsSessionDO>()
                .eq(CsSessionDO::getSessionId, sessionId)
                .set(CsSessionDO::getAgentId, targetBotId));
    }
}
