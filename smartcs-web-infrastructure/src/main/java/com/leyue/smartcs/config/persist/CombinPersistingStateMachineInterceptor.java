package com.leyue.smartcs.config.persist;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.util.Assert;


@RequiredArgsConstructor
public class CombinPersistingStateMachineInterceptor<S, E, T> extends AbstractPersistingStateMachineInterceptor<S, E, T> implements StateMachineRuntimePersister<S, E, T> {
    private final CombinRepositoryStateMachinePersist<S, E> persist;
    
    public CombinPersistingStateMachineInterceptor(JpaStateMachineRepository jpaStateMachineRepository, RedissonClient redissonClient) {
        Assert.notNull(jpaStateMachineRepository, "'jpaStateMachineRepository' must be set");
        this.persist = new CombinRepositoryStateMachinePersist<S, E>(jpaStateMachineRepository, redissonClient);
    }

    @Override
    public StateMachineInterceptor<S, E> getInterceptor() {
        return this;
    }

    @Override
    public void write(StateMachineContext<S, E> context, T contextObj) throws Exception {
        persist.write(context, contextObj);
    }

    @Override
    public StateMachineContext<S, E> read(Object contextObj) throws Exception {
        return persist.read(contextObj);
    }
}