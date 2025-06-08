package com.leyue.smartcs.chat.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.enums.SessionEvent;
import com.leyue.smartcs.domain.chat.enums.SessionState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 会话状态机服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionStateMachineService {

    private final StateMachineFactory<SessionState, SessionEvent> stateMachineFactory;
    private final WebSocketSessionManager webSocketSessionManager;
    
    // 会话状态机缓存
    private final Map<String, StateMachine<SessionState, SessionEvent>> stateMachines = new ConcurrentHashMap<>();

    /**
     * 创建会话状态机
     */
    public StateMachine<SessionState, SessionEvent> createStateMachine(Session session) {
        StateMachine<SessionState, SessionEvent> stateMachine = stateMachineFactory.getStateMachine(String.valueOf(session.getSessionId()));
        stateMachine.stop();
        
        // 设置状态机初始状态
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> {
                    accessor.resetStateMachine(new DefaultStateMachineContext<>(
                            session.getSessionState(),
                            null, 
                            null, 
                            null));
                });
        
        // 设置状态机变量
        stateMachine.getExtendedState().getVariables().put("session", session);
        
        // 启动状态机
        stateMachine.start();
        
        // 缓存状态机
        stateMachines.put(String.valueOf(session.getSessionId()), stateMachine);
        
        return stateMachine;
    }

    /**
     * 获取会话状态机，如果不存在则创建
     */
    public StateMachine<SessionState, SessionEvent> getStateMachine(Session session) {
        return stateMachines.computeIfAbsent(session.getSessionId().toString(), key -> createStateMachine(session));
    }

    /**
     * 发送事件到状态机
     */
    public boolean sendEvent(Session session, SessionEvent event, Map<String, Object> variables) {
        StateMachine<SessionState, SessionEvent> stateMachine = getStateMachine(session);
        
        if (variables != null) {
            variables.forEach((key, value) -> 
                    stateMachine.getExtendedState().getVariables().put(key, value));
        }
        
        boolean result = stateMachine.sendEvent(MessageBuilder
                .withPayload(event)
                .build());
        
        if (result) {
            // 更新会话状态
            SessionState newStatus = stateMachine.getState().getId();
            session.setSessionState(newStatus);
            
            // 发送状态变更通知
            sendStatusChangeNotification(session);
        }
        
        return result;
    }
    
    /**
     * 发送状态变更通知
     */
    private void sendStatusChangeNotification(Session session) {
        SessionStateMessage statusMessage = new SessionStateMessage();
        statusMessage.setSessionId(session.getSessionId().toString());
        statusMessage.setStatus(session.getSessionState().name());
        statusMessage.setStatusChangeTime(System.currentTimeMillis());
        
        if (SessionState.ACTIVE.equals(session.getSessionState())) {
            statusMessage.setAgentId(session.getAgentId().toString());
            statusMessage.setAgentName(session.getAgentName());
        } else if (SessionState.CLOSED.equals(session.getSessionState())) {
            statusMessage.setCloseReason(session.getCloseReason());
        }
        
        // 发送给客户
        if (session.getCustomerId() != null) {
            webSocketSessionManager.sendToUser(session.getCustomerId().toString(), "status", statusMessage);
        }
        
        // 发送给客服
        if (session.getAgentId() != null) {
            webSocketSessionManager.sendToUser(session.getAgentId().toString(), "status", statusMessage);
        }
    }
    
    /**
     * 分配客服
     */
    public boolean assignAgent(Session session, String agentId, String agentName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("agentId", Long.valueOf(agentId));
        variables.put("agentName", agentName);
        variables.put("startTime", System.currentTimeMillis());
        
        return sendEvent(session, SessionEvent.ASSIGN, variables);
    }
    
    /**
     * 关闭会话
     */
    public boolean closeSession(Session session, String reason) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("closeReason", reason);
        variables.put("endTime", System.currentTimeMillis());
        
        return sendEvent(session, SessionEvent.CLOSE, variables);
    }
}
