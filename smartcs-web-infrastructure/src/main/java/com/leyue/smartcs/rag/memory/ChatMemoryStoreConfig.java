package com.leyue.smartcs.rag.memory;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatMemoryStoreConfig {
    @Bean
    public ChatMemoryStore redisChatMemoryStore(RedisProperties redisProperties){
        return new RedisChatMemoryStore(redisProperties.getHost(), redisProperties.getPort(),redisProperties.getUsername(),redisProperties.getPassword());
    }

    @Bean
    @Primary
    public ChatMemoryStore chatMemoryStore(ChatMemoryStore redisChatMemoryStore, RedissonClient redissonClient){
        return new FaultTolerantRedisChatMemoryStore(redisChatMemoryStore, redissonClient);
    }
}
