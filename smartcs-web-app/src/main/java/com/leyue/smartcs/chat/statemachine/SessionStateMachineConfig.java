package com.leyue.smartcs.chat.statemachine;

import com.leyue.smartcs.domain.chat.SessionEvent;
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
@Configuration
@EnableStateMachineFactory(name = "sessionStateMachineFactory")
public class SessionStateMachineConfig extends EnumStateMachineConfigurerAdapter<SessionStatus, SessionEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<SessionStatus, SessionEvent> states) throws Exception {
        states
            .withStates()
                .initial(SessionStatus.WAITING)
                .states(EnumSet.allOf(SessionStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<SessionStatus, SessionEvent> transitions) throws Exception {
        transitions
            .withExternal()
                .source(SessionStatus.WAITING)
                .target(SessionStatus.ACTIVE)
                .event(SessionEvent.ASSIGN)
                .and()
            .withExternal()
                .source(SessionStatus.WAITING)
                .target(SessionStatus.CLOSED)
                .event(SessionEvent.CLOSE)
                .and()
            .withExternal()
                .source(SessionStatus.ACTIVE)
                .target(SessionStatus.CLOSED)
                .event(SessionEvent.CLOSE);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<SessionStatus, SessionEvent> config) throws Exception {
        config
            .withConfiguration()
                .autoStartup(true)
                .listener(sessionStateChangeListener());
    }
    
    @Bean
    public StateMachineListener<SessionStatus, SessionEvent> sessionStateChangeListener() {
        return new StateMachineListenerAdapter<SessionStatus, SessionEvent>() {
            @Override
            public void stateChanged(State<SessionStatus, SessionEvent> from, State<SessionStatus, SessionEvent> to) {
                if (from != null && to != null) {
                    System.out.println("会话状态变更：" + from.getId() + " -> " + to.getId());
                }
            }
        };
    }
}
