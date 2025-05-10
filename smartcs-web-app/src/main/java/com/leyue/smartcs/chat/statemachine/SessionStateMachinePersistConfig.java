package com.leyue.smartcs.chat.statemachine;

import com.leyue.smartcs.config.persist.CombinPersistingStateMachineInterceptor;
import com.leyue.smartcs.domain.chat.SessionEvent;
import com.leyue.smartcs.domain.chat.SessionState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

/**
 * 订单状态机持久化配置
 * 用于持久化状态机状态到Redis和数据库
 * 支持分布式环境下的状态机状态共享
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SessionStateMachinePersistConfig {
    @Bean
    public StateMachineRuntimePersister<SessionState, SessionEvent, String> statesSessionEventStringStateMachineRuntimePersister(JpaStateMachineRepository jpaStateMachineRepository, RedissonClient redissonClient) {
        return new CombinPersistingStateMachineInterceptor<>(jpaStateMachineRepository,redissonClient);
    }

    @Bean
    public StateMachineService<SessionState, SessionEvent> SessionEventStateMachineService(
            StateMachineFactory<SessionState, SessionEvent> factory,
            StateMachineRuntimePersister<SessionState, SessionEvent, String> persist) {
        return new DefaultStateMachineService<>(factory, persist);
    }
} 