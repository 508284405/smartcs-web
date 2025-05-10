package com.leyue.smartcs.config.persist;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.data.redis.RedisRepositoryStateMachine;
import org.springframework.statemachine.kryo.KryoStateMachineSerialisationService;
import org.springframework.statemachine.service.StateMachineSerialisationService;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedissonStateMachinePersist<S, E> implements StateMachinePersist<S, E, Object> {

    private final StateMachineSerialisationService<S, E> serialisationService;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new repository state machine persist.
     */
    protected RedissonStateMachinePersist(RedissonClient redissonClient) {
        objectMapper = new ObjectMapper();
        this.redissonClient = redissonClient;
        this.serialisationService = new KryoStateMachineSerialisationService<>();
    }

    /**
     * Instantiates a new repository state machine persist.
     *
     * @param serialisationService
     *     the serialisation service
     */
    protected RedissonStateMachinePersist(StateMachineSerialisationService<S, E> serialisationService, RedissonClient redissonClient) {
        objectMapper = new ObjectMapper();
        this.redissonClient = redissonClient;
        this.serialisationService = serialisationService;
    }

    @Override
    public void write(StateMachineContext<S, E> context, Object contextObj) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Persisting context " + context + " using contextObj " + contextObj);
        }
        RedisRepositoryStateMachine build = build(context, contextObj, serialisationService.serialiseStateMachineContext(context));
        String value = objectMapper.writeValueAsString(build);
        RBucket<String> bucket = redissonClient.getBucket(getName(contextObj.toString()));
        bucket.expire(Duration.ofSeconds(60));
        bucket.set(value);
    }

    @Override
    public StateMachineContext<S, E> read(Object contextObj) throws Exception {
        RBucket<String> bucket = redissonClient.getBucket(getName(contextObj.toString()));
        if (bucket.get() == null) {
            return null;
        }
        RedisRepositoryStateMachine repositoryStateMachine = objectMapper.readValue(bucket.get(), RedisRepositoryStateMachine.class);
        // use child contexts if we have those, otherwise fall back to child context refs.
        StateMachineContext<S, E> context = serialisationService.deserialiseStateMachineContext(repositoryStateMachine.getStateMachineContext());
        ;
        if (context != null && context.getChilds() != null && context.getChilds().isEmpty() && context.getChildReferences() != null) {
            List<StateMachineContext<S, E>> contexts = new ArrayList<>();
            for (String childRef : context.getChildReferences()) {
                RBucket<RedisRepositoryStateMachine> childRefBucket = redissonClient.getBucket(getName(childRef));
                if (childRefBucket.get() != null) {
                    repositoryStateMachine = objectMapper.readValue(bucket.get(), RedisRepositoryStateMachine.class);
                    // use child contexts if we have those, otherwise fall back to child context refs.
                    contexts.add(serialisationService.deserialiseStateMachineContext(repositoryStateMachine.getStateMachineContext()));
                }
            }
            return new DefaultStateMachineContext<S, E>(contexts, context.getState(), context.getEvent(), context.getEventHeaders(), context.getExtendedState(),
                context.getHistoryStates(), context.getId());
        } else {
            return context;
        }
    }

    private static String getName(String childRef) {
        return "RedisRepositoryStateMachine:" + childRef;
    }

    protected RedisRepositoryStateMachine build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext) {
        RedisRepositoryStateMachine redisRepositoryStateMachine = new RedisRepositoryStateMachine();
        redisRepositoryStateMachine.setId(contextObj.toString());
        redisRepositoryStateMachine.setMachineId(context.getId());
        redisRepositoryStateMachine.setState(context.getState().toString());
        redisRepositoryStateMachine.setStateMachineContext(serialisedContext);
        return redisRepositoryStateMachine;
    }
}