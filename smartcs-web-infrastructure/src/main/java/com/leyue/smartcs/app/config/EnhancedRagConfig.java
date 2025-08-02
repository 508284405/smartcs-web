package com.leyue.smartcs.app.config;

import com.leyue.smartcs.app.service.AiAppChatServiceFactory;
import com.leyue.smartcs.app.tools.ToolManager;
import com.leyue.smartcs.app.observability.ChatMetricsCollector;
import com.leyue.smartcs.domain.app.service.StructuredChatService;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 增强RAG系统配置
 * 统一配置所有RAG相关组件
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EnhancedRagConfig {

    private final AiAppChatServiceFactory chatServiceFactory;
    private final ToolManager toolManager;
    private final ChatMetricsCollector metricsCollector;

    /**
     * 创建结构化聊天服务
     * 用于生成结构化输出
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.ai.structured-output.enabled", havingValue = "true", matchIfMissing = true)
    public StructuredChatService structuredChatService(ChatModel chatModel) {
        log.info("创建结构化聊天服务");
        
        return AiServices.builder(StructuredChatService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 预热RAG系统组件
     */
    @Bean
    @DependsOn({"chatServiceFactory", "toolManager", "metricsCollector"})
    public RagSystemWarmer ragSystemWarmer() {
        return new RagSystemWarmer(chatServiceFactory, toolManager, metricsCollector);
    }

    /**
     * RAG系统预热组件
     */
    @RequiredArgsConstructor
    public static class RagSystemWarmer {
        private final AiAppChatServiceFactory chatServiceFactory;
        private final ToolManager toolManager;
        private final ChatMetricsCollector metricsCollector;

        /**
         * 预热系统组件
         */
        public void warmup() {
            try {
                log.info("开始预热RAG系统组件");
                
                // 预热工具管理器
                toolManager.warmupTools();
                
                // 初始化指标收集器
                metricsCollector.getMetricsSummary();
                
                log.info("RAG系统组件预热完成");
            } catch (Exception e) {
                log.error("RAG系统预热失败", e);
            }
        }
    }
}