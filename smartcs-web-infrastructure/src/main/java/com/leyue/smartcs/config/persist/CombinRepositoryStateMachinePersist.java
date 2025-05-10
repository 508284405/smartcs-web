package com.leyue.smartcs.config.persist;

import org.redisson.api.RedissonClient;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.RepositoryStateMachine;
import org.springframework.statemachine.data.RepositoryStateMachinePersist;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachinePersist;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;

public class CombinRepositoryStateMachinePersist<S,E> extends RepositoryStateMachinePersist<RepositoryStateMachine, S,E> {
    private final JpaRepositoryStateMachinePersist<S, E> jpaRepositoryStateMachinePersist;
    private final RedissonStateMachinePersist<S, E> redissonStateMachinePersist;

    public CombinRepositoryStateMachinePersist(JpaStateMachineRepository jpaStateMachineRepository, RedissonClient redissonClient) {
        this.jpaRepositoryStateMachinePersist = new JpaRepositoryStateMachinePersist<>(jpaStateMachineRepository);
        this.redissonStateMachinePersist = new RedissonStateMachinePersist<>(redissonClient);
    }

    @Override
    public void write(StateMachineContext<S, E> context, Object contextObj) throws Exception {
        redissonStateMachinePersist.write(context, contextObj);
        jpaRepositoryStateMachinePersist.write(context, contextObj);
    }

    @Override
    public StateMachineContext<S, E> read(Object contextObj) throws Exception {
        StateMachineContext<S, E> context = redissonStateMachinePersist.read(contextObj);
        if(context == null){
            context = jpaRepositoryStateMachinePersist.read(contextObj);
        }
        return context;
    }

    @Override
    protected StateMachineRepository<RepositoryStateMachine> getRepository() {
        return null;
    }

    @Override
    protected JpaRepositoryStateMachine build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext) {
        return null;
    }
}