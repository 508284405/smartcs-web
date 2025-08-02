package com.leyue.smartcs.app.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天事件监听器
 * 监听聊天相关事件并记录日志和指标
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener {

    private final ChatMetricsCollector metricsCollector;

    /**
     * 监听聊天开始事件
     */
    @EventListener
    public void handleChatStartEvent(ChatStartEvent event) {
        log.info("聊天开始: sessionId={}, appId={}, modelKey={}, ragEnabled={}, toolsEnabled={}", 
                event.getSessionId(), event.getAppId(), event.getModelKey(), 
                event.isRagEnabled(), event.isToolsEnabled());
        
        // 记录指标
        event.setSample(metricsCollector.recordChatStart());
    }

    /**
     * 监听聊天完成事件
     */
    @EventListener
    public void handleChatCompleteEvent(ChatCompleteEvent event) {
        log.info("聊天完成: sessionId={}, successful={}, duration={}ms, responseLength={}", 
                event.getSessionId(), event.isSuccessful(), event.getDuration(), 
                event.getResponseLength());
        
        // 记录指标
        if (event.getSample() != null) {
            metricsCollector.recordChatComplete(
                event.getSample(), 
                event.isSuccessful(), 
                event.getModelKey(),
                event.isRagEnabled(),
                event.isToolsUsed()
            );
        }
        
        if (event.getResponseLength() > 0) {
            metricsCollector.recordResponseLength(event.getResponseLength());
        }
    }

    /**
     * 监听RAG检索事件
     */
    @EventListener
    public void handleRagRetrievalEvent(RagRetrievalEvent event) {
        log.debug("RAG检索: sessionId={}, query={}, retrievedCount={}, duration={}ms", 
                 event.getSessionId(), event.getQuery(), event.getRetrievedCount(), event.getDuration());
        
        if (event.getSample() != null) {
            metricsCollector.recordRagRetrievalComplete(event.getSample(), event.getRetrievedCount());
        }
    }

    /**
     * 监听工具使用事件
     */
    @EventListener
    public void handleToolUsageEvent(ToolUsageEvent event) {
        log.info("工具使用: tool={}, sessionId={}, successful={}, duration={}ms", 
                event.getToolName(), event.getSessionId(), event.isSuccessful(), event.getDuration());
        
        metricsCollector.recordToolUsage(event.getToolName(), event.isSuccessful());
    }

    /**
     * 监听聊天错误事件
     */
    @EventListener
    public void handleChatErrorEvent(ChatErrorEvent event) {
        log.error("聊天错误: sessionId={}, errorType={}, component={}, message={}", 
                 event.getSessionId(), event.getErrorType(), event.getComponent(), 
                 event.getErrorMessage(), event.getThrowable());
        
        metricsCollector.recordError(event.getErrorType(), event.getComponent());
    }

    /**
     * 监听会话统计事件
     */
    @EventListener
    public void handleSessionStatsEvent(SessionStatsEvent event) {
        log.info("会话统计: sessionId={}, messageCount={}, duration={}ms", 
                event.getSessionId(), event.getMessageCount(), event.getDurationMillis());
        
        metricsCollector.recordSessionStats(
            event.getSessionId(), 
            event.getMessageCount(), 
            java.time.Duration.ofMillis(event.getDurationMillis())
        );
    }

    // 事件类定义

    /**
     * 聊天开始事件
     */
    @lombok.Data
    @lombok.Builder
    public static class ChatStartEvent {
        private String sessionId;
        private Long appId;
        private String modelKey;
        private boolean ragEnabled;
        private boolean toolsEnabled;
        private LocalDateTime timestamp;
        private io.micrometer.core.instrument.Timer.Sample sample;
    }

    /**
     * 聊天完成事件
     */
    @lombok.Data
    @lombok.Builder
    public static class ChatCompleteEvent {
        private String sessionId;
        private boolean successful;
        private long duration;
        private int responseLength;
        private String modelKey;
        private boolean ragEnabled;
        private boolean toolsUsed;
        private LocalDateTime timestamp;
        private io.micrometer.core.instrument.Timer.Sample sample;
    }

    /**
     * RAG检索事件
     */
    @lombok.Data
    @lombok.Builder
    public static class RagRetrievalEvent {
        private String sessionId;
        private String query;
        private int retrievedCount;
        private long duration;
        private LocalDateTime timestamp;
        private io.micrometer.core.instrument.Timer.Sample sample;
    }

    /**
     * 工具使用事件
     */
    @lombok.Data
    @lombok.Builder
    public static class ToolUsageEvent {
        private String sessionId;
        private String toolName;
        private boolean successful;
        private long duration;
        private Map<String, Object> parameters;
        private LocalDateTime timestamp;
    }

    /**
     * 聊天错误事件
     */
    @lombok.Data
    @lombok.Builder
    public static class ChatErrorEvent {
        private String sessionId;
        private String errorType;
        private String component;
        private String errorMessage;
        private Throwable throwable;
        private LocalDateTime timestamp;
    }

    /**
     * 会话统计事件
     */
    @lombok.Data
    @lombok.Builder
    public static class SessionStatsEvent {
        private String sessionId;
        private int messageCount;
        private long durationMillis;
        private LocalDateTime timestamp;
    }
}