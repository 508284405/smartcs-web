package com.leyue.smartcs.app.config;

import com.leyue.smartcs.app.service.AiAppChatServiceImpl;
import com.leyue.smartcs.app.service.StructuredChatServiceImpl;
import com.leyue.smartcs.domain.app.service.AiAppChatService;
import com.leyue.smartcs.domain.app.service.StructuredChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Domain服务配置
 * 将Domain层接口与Infrastructure层实现进行绑定
 */
@Configuration
@Slf4j
public class DomainServiceConfig {

    /**
     * 配置AI应用聊天领域服务
     * 使用Infrastructure层的LangChain4j实现
     */
    @Bean("domainAiAppChatService")
    @Primary
    @ConditionalOnMissingBean(name = "domainAiAppChatService")
    public AiAppChatService aiAppChatService(AiAppChatServiceImpl implementation) {
        log.info("配置AI应用聊天领域服务: 使用LangChain4j技术实现");
        return implementation;
    }

    /**
     * 配置结构化聊天领域服务
     * 使用Infrastructure层的LangChain4j实现
     */
    @Bean("domainStructuredChatService")
    @Primary
    @ConditionalOnMissingBean(name = "domainStructuredChatService")
    public StructuredChatService structuredChatService(StructuredChatServiceImpl implementation) {
        log.info("配置结构化聊天领域服务: 使用LangChain4j技术实现");
        return implementation;
    }
}