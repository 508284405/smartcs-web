package com.leyue.smartcs.rag.memory;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;

import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService;

@Configuration
public class ChatMemoryStoreConfig {
    @Bean
    public ChatMemoryStore redisChatMemoryStore(RedisProperties redisProperties){
        return new RedisChatMemoryStore(redisProperties.getHost(), redisProperties.getPort(),redisProperties.getUsername(),redisProperties.getPassword());
    }

    @Bean
    @Primary
    public ChatMemoryStore chatMemoryStore(
            ChatMemoryStore redisChatMemoryStore,
            RedissonClient redissonClient,
            @org.springframework.beans.factory.annotation.Autowired(required = false) LTMDomainService ltmDomainService,
            @Value("${smartcs.ai.ltm.retrieval.enabled:true}") boolean ltmEnabled,
            @Value("${smartcs.ai.ltm.context.chat-store.enabled:false}") boolean ltmChatStoreEnabled
    ){
        ChatMemoryStore base = new FaultTolerantRedisChatMemoryStore(redisChatMemoryStore, redissonClient);
        // 可选启用LTM增强的ChatMemoryStore：在现有容错基础上包一层LTM上下文能力
        if (ltmEnabled && ltmChatStoreEnabled && ltmDomainService != null) {
            return new LTMEnhancedRedisChatMemoryStore(base, ltmDomainService);
        }
        return base;
    }
}
