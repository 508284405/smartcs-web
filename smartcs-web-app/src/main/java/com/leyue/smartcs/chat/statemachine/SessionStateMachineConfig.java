package com.leyue.smartcs.chat.statemachine;

import com.leyue.smartcs.domain.chat.enums.SessionEvent;
import com.leyue.smartcs.domain.chat.enums.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * 会话状态机配置
 */
@Slf4j
@Configuration
@EnableStateMachineFactory(name = "sessionStateMachineFactory")
public class SessionStateMachineConfig extends EnumStateMachineConfigurerAdapter<SessionState, SessionEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<SessionState, SessionEvent> states) throws Exception {
        states
            .withStates()
                .initial(SessionState.WAITING)
                .states(EnumSet.allOf(SessionState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<SessionState, SessionEvent> transitions) throws Exception {
        transitions
            .withExternal()
                .source(SessionState.WAITING)
                .target(SessionState.ACTIVE)
                .event(SessionEvent.ASSIGN)
                .and()
            .withExternal()
                .source(SessionState.WAITING)
                .target(SessionState.CLOSED)
                .event(SessionEvent.CLOSE)
                .and()
            .withExternal()
                .source(SessionState.ACTIVE)
                .target(SessionState.CLOSED)
                .event(SessionEvent.CLOSE);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<SessionState, SessionEvent> config) throws Exception {
        config
            .withConfiguration()
                .autoStartup(true)
                .listener(sessionStateChangeListener());
    }
    
    @Bean
    public StateMachineListener<SessionState, SessionEvent> sessionStateChangeListener() {
        return new StateMachineListenerAdapter<SessionState, SessionEvent>() {
            @Override
            public void stateChanged(State<SessionState, SessionEvent> from, State<SessionState, SessionEvent> to) {
                if (from != null && to != null) {
                    log.info("会话状态变更：{} -> {}", from.getId(), to.getId());
                }
            }
        };
    }
}
