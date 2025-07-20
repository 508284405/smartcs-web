package com.leyue.smartcs.knowledge.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天记忆配置 - 使用LangChain4j实现
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 聊天记忆
     * @return 聊天记忆
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}
