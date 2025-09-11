package com.leyue.smartcs.rag.observability;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天指标收集器
 * 基于Micrometer收集聊天相关的指标数据
 */
@Component
@Slf4j
public class ChatMetricsCollector {

    private final MeterRegistry meterRegistry;

    // 计数器
    private final Counter totalChatsCounter;
    private final Counter successfulChatsCounter;
    private final Counter failedChatsCounter;
    private final Counter ragChatsCounter;
    private final Counter toolUsageCounter;

    // 计时器
    private final Timer chatDurationTimer;
    private final Timer ragRetrievalTimer;

    // 仪表盘指标
    private final AtomicLong activeChatsGauge = new AtomicLong(0);
    private final AtomicLong memoryUsageGauge = new AtomicLong(0);
    private final ConcurrentHashMap<String, LongAdder> modelUsageCounters = new ConcurrentHashMap<>();

    public ChatMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.totalChatsCounter = Counter.builder("smartcs.chat.total")
                .description("Total number of chat requests")
                .register(meterRegistry);
        
        this.successfulChatsCounter = Counter.builder("smartcs.chat.successful")
                .description("Number of successful chat requests")
                .register(meterRegistry);
        
        this.failedChatsCounter = Counter.builder("smartcs.chat.failed")
                .description("Number of failed chat requests")
                .register(meterRegistry);
        
        this.ragChatsCounter = Counter.builder("smartcs.chat.rag")
                .description("Number of RAG-enabled chat requests")
                .register(meterRegistry);
        
        this.toolUsageCounter = Counter.builder("smartcs.chat.tool_usage")
                .description("Number of tool usages in chats")
                .register(meterRegistry);
        
        // 初始化计时器
        this.chatDurationTimer = Timer.builder("smartcs.chat.duration")
                .description("Duration of chat requests")
                .register(meterRegistry);
        
        this.ragRetrievalTimer = Timer.builder("smartcs.chat.rag.retrieval_duration")
                .description("Duration of RAG content retrieval")
                .register(meterRegistry);
        
        // 注册仪表盘指标
        Gauge.builder("smartcs.chat.active", activeChatsGauge, AtomicLong::get)
                .description("Number of active chat sessions")
                .register(meterRegistry);

        Gauge.builder("smartcs.chat.memory_usage", memoryUsageGauge, AtomicLong::get)
                .description("Memory usage in bytes")
                .register(meterRegistry);
    }

    /**
     * 记录聊天请求开始
     */
    public Timer.Sample recordChatStart() {
        totalChatsCounter.increment();
        activeChatsGauge.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    /**
     * 记录聊天请求完成
     */
    public void recordChatComplete(Timer.Sample sample, boolean successful, String modelKey, boolean ragEnabled, boolean toolsUsed) {
        sample.stop(chatDurationTimer);
        activeChatsGauge.decrementAndGet();
        
        if (successful) {
            successfulChatsCounter.increment();
        } else {
            failedChatsCounter.increment();
        }

        if (ragEnabled) {
            ragChatsCounter.increment();
        }

        if (toolsUsed) {
            toolUsageCounter.increment();
        }

        // 记录模型使用情况
        modelUsageCounters.computeIfAbsent(modelKey, k -> {
            LongAdder adder = new LongAdder();
            Gauge.builder("smartcs.chat.model_usage", () -> adder.sum())
                    .description("Usage count by model")
                    .tag("model", k)
                    .register(meterRegistry);
            return adder;
        }).increment();
    }

    /**
     * 记录RAG检索时间
     */
    public Timer.Sample recordRagRetrievalStart() {
        return Timer.start(meterRegistry);
    }

    /**
     * 记录RAG检索完成
     */
    public void recordRagRetrievalComplete(Timer.Sample sample, int retrievedCount) {
        sample.stop(ragRetrievalTimer);
        
        Counter.builder("smartcs.chat.rag.retrieved_contents")
                .description("Number of contents retrieved by RAG")
                .register(meterRegistry)
                .increment(retrievedCount);
    }

    /**
     * 记录工具使用
     */
    public void recordToolUsage(String toolName, boolean successful) {
        Counter.builder("smartcs.chat.tool.usage")
                .description("Tool usage count")
                .tag("tool", toolName)
                .tag("status", successful ? "success" : "failure")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录响应长度
     */
    public void recordResponseLength(int length) {
        meterRegistry.summary("smartcs.chat.response_length")
                .record(length);
    }

    /**
     * 记录内存使用
     */
    public void updateMemoryUsage(long memoryBytes) {
        memoryUsageGauge.set(memoryBytes);
    }

    /**
     * 记录错误
     */
    public void recordError(String errorType, String component) {
        Counter.builder("smartcs.chat.errors")
                .description("Number of errors by type and component")
                .tag("error_type", errorType)
                .tag("component", component)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录会话统计
     */
    public void recordSessionStats(String sessionId, int messageCount, Duration sessionDuration) {
        meterRegistry.summary("smartcs.chat.session.message_count")
                .record(messageCount);

        meterRegistry.timer("smartcs.chat.session.duration")
                .record(sessionDuration);
    }

    /**
     * 记录知识库使用
     */
    public void recordKnowledgeBaseUsage(Long knowledgeBaseId, int queriesCount) {
        Counter.builder("smartcs.chat.knowledge_base.usage")
                .description("Knowledge base usage count")
                .tag("kb_id", knowledgeBaseId.toString())
                .register(meterRegistry)
                .increment(queriesCount);
    }

    /**
     * 获取指标摘要
     */
    public ChatMetricsSummary getMetricsSummary() {
        return ChatMetricsSummary.builder()
                .totalChats((long) totalChatsCounter.count())
                .successfulChats((long) successfulChatsCounter.count())
                .failedChats((long) failedChatsCounter.count())
                .ragChats((long) ragChatsCounter.count())
                .toolUsages((long) toolUsageCounter.count())
                .activeChats(activeChatsGauge.get())
                .averageChatDuration(chatDurationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
                .averageRagRetrievalDuration(ragRetrievalTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
                .memoryUsage(memoryUsageGauge.get())
                .build();
    }

    /**
     * 指标摘要数据类
     */
    @lombok.Builder
    @lombok.Data
    public static class ChatMetricsSummary {
        private Long totalChats;
        private Long successfulChats;
        private Long failedChats;
        private Long ragChats;
        private Long toolUsages;
        private Long activeChats;
        private Double averageChatDuration;
        private Double averageRagRetrievalDuration;
        private Long memoryUsage;
    }
}