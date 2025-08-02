package com.leyue.smartcs.app.config;

import com.leyue.smartcs.app.memory.FaultTolerantRedisChatMemoryStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI应用聊天助手配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppChatAssistantConfig {

    private final FaultTolerantRedisChatMemoryStore faultTolerantRedisChatMemoryStore;

    /**
     * 聊天记忆存储 - 使用容错Redis实现分布式持久化
     * 支持自动降级到InMemory存储
     */
    @Bean
    @Primary
    public ChatMemoryStore chatMemoryStore() {
        log.info("创建容错Redis聊天记忆存储");
        return faultTolerantRedisChatMemoryStore;
    }
}