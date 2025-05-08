package com.leyue.smartcs.chat.statemachine;

import com.leyue.smartcs.api.chat.dto.websocket.SessionStatusMessage;
import com.leyue.smartcs.chat.model.Session;
import com.leyue.smartcs.domain.chat.SessionEvent;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话状态机服务
 */
@Slf4j
@Service
public class SessionStateMachineService {

    private final StateMachineFactory<SessionStatus, SessionEvent> stateMachineFactory;
    private final WebSocketSessionManager webSocketSessionManager;
    
    // 会话状态机缓存
    private final Map<String, StateMachine<SessionStatus, SessionEvent>> stateMachines = new ConcurrentHashMap<>();

    @Autowired
    public SessionStateMachineService(
            StateMachineFactory<SessionStatus, SessionEvent> stateMachineFactory,
            WebSocketSessionManager webSocketSessionManager) {
        this.stateMachineFactory = stateMachineFactory;
        this.webSocketSessionManager = webSocketSessionManager;
    }

    /**
     * 创建会话状态机
     */
    public StateMachine<SessionStatus, SessionEvent> createStateMachine(Session session) {
        StateMachine<SessionStatus, SessionEvent> stateMachine = stateMachineFactory.getStateMachine(session.getSessionId());
        stateMachine.stop();
        
        // 设置状态机初始状态
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> {
                    accessor.resetStateMachine(new DefaultStateMachineContext<>(
                            SessionStatus.valueOf(session.getStatus()), 
                            null, 
                            null, 
                            null));
                });
        
        // 设置状态机变量
        stateMachine.getExtendedState().getVariables().put("session", session);
        
        // 启动状态机
        stateMachine.start();
        
        // 缓存状态机
        stateMachines.put(session.getSessionId(), stateMachine);
        
        return stateMachine;
    }

    /**
     * 获取会话状态机，如果不存在则创建
     */
    public StateMachine<SessionStatus, SessionEvent> getStateMachine(Session session) {
        return stateMachines.computeIfAbsent(session.getSessionId(), key -> createStateMachine(session));
    }

    /**
     * 发送事件到状态机
     */
    public boolean sendEvent(Session session, SessionEvent event, Map<String, Object> variables) {
        StateMachine<SessionStatus, SessionEvent> stateMachine = getStateMachine(session);
        
        if (variables != null) {
            variables.forEach((key, value) -> 
                    stateMachine.getExtendedState().getVariables().put(key, value));
        }
        
        boolean result = stateMachine.sendEvent(MessageBuilder
                .withPayload(event)
                .build());
        
        if (result) {
            // 更新会话状态
            SessionStatus newStatus = stateMachine.getState().getId();
            session.setStatus(newStatus.name());
            
            // 发送状态变更通知
            sendStatusChangeNotification(session);
        }
        
        return result;
    }
    
    /**
     * 发送状态变更通知
     */
    private void sendStatusChangeNotification(Session session) {
        SessionStatusMessage statusMessage = new SessionStatusMessage();
        statusMessage.setSessionId(session.getSessionId());
        statusMessage.setStatus(session.getStatus());
        statusMessage.setStatusChangeTime(System.currentTimeMillis());
        
        if (SessionStatus.ACTIVE.name().equals(session.getStatus())) {
            statusMessage.setAgentId(session.getAgentId());
            statusMessage.setAgentName(session.getAgentName());
        } else if (SessionStatus.CLOSED.name().equals(session.getStatus())) {
            statusMessage.setCloseReason(session.getCloseReason());
        }
        
        // 发送给客户
        if (session.getCustomerId() != null) {
            webSocketSessionManager.sendToUser(session.getCustomerId(), "status", statusMessage);
        }
        
        // 发送给客服
        if (session.getAgentId() != null) {
            webSocketSessionManager.sendToUser(session.getAgentId(), "status", statusMessage);
        }
    }
    
    /**
     * 分配客服
     */
    public boolean assignAgent(Session session, String agentId, String agentName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("agentId", agentId);
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
