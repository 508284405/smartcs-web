package com.leyue.smartcs.chat.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IM系统业务指标收集器
 * 
 * @author Claude  
 * @since 2024-08-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    // 核心业务指标
    private Counter messagesSentTotal;
    private Counter messagesDeliveredTotal;
    private Counter messagesFailedTotal;
    private Counter messagesRecalledTotal;
    private Counter usersOnlineTotal;
    private Timer messageDeliveryLatency;
    private Timer messageProcessingTime;
    
    // 分类指标
    private final ConcurrentHashMap<String, Counter> messageTypeCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> messageTypeLatencies = new ConcurrentHashMap<>();
    
    // 实时统计
    private final AtomicLong totalActiveConnections = new AtomicLong(0);
    private final AtomicLong totalMessagesInQueue = new AtomicLong(0);
    
    @PostConstruct
    public void initializeMetrics() {
        // 基础业务指标
        messagesSentTotal = Counter.builder("im.messages.sent.total")
                .description("Total number of messages sent")
                .register(meterRegistry);
                
        messagesDeliveredTotal = Counter.builder("im.messages.delivered.total")
                .description("Total number of messages successfully delivered")
                .register(meterRegistry);
                
        messagesFailedTotal = Counter.builder("im.messages.failed.total")
                .description("Total number of failed message deliveries")
                .tag("reason", "unknown")
                .register(meterRegistry);
                
        messagesRecalledTotal = Counter.builder("im.messages.recalled.total")
                .description("Total number of messages recalled")
                .register(meterRegistry);
                
        usersOnlineTotal = Counter.builder("im.users.online.total")
                .description("Total number of online users")
                .register(meterRegistry);
        
        // 延迟指标
        messageDeliveryLatency = Timer.builder("im.message.delivery.latency")
                .description("Message delivery latency")
                .register(meterRegistry);
                
        messageProcessingTime = Timer.builder("im.message.processing.time")
                .description("Message processing time")
                .register(meterRegistry);
        
        // 实时连接数量指标
        meterRegistry.gauge("im.connections.active", totalActiveConnections);
        meterRegistry.gauge("im.messages.queue.size", totalMessagesInQueue);
        
        log.info("IM业务指标初始化完成");
    }
    
    /**
     * 记录消息发送
     */
    public void recordMessageSent(String messageType, String sessionType) {
        messagesSentTotal.increment();
        getMessageTypeCounter("sent", messageType).increment();
        
        // 按会话类型统计
        meterRegistry.counter("im.messages.sent.by.session", 
                "session_type", sessionType).increment();
    }
    
    /**
     * 记录消息投递成功
     */
    public void recordMessageDelivered(String messageType, Duration latency) {
        messagesDeliveredTotal.increment();
        messageDeliveryLatency.record(latency);
        getMessageTypeCounter("delivered", messageType).increment();
    }
    
    /**
     * 记录消息投递失败
     */
    public void recordMessageFailed(String messageType, String failureReason) {
        meterRegistry.counter("im.messages.failed.total",
                "message_type", messageType,
                "reason", failureReason).increment();
    }
    
    /**
     * 记录消息撤回
     */
    public void recordMessageRecalled(String messageType, Duration messageAge) {
        messagesRecalledTotal.increment();
        meterRegistry.timer("im.messages.recall.latency",
                "message_type", messageType).record(messageAge);
    }
    
    /**
     * 记录消息处理时间
     */
    public void recordMessageProcessingTime(String operation, Duration processingTime) {
        meterRegistry.timer("im.message.processing.time",
                "operation", operation).record(processingTime);
    }
    
    /**
     * 记录用户上线
     */
    public void recordUserOnline(String userType) {
        meterRegistry.counter("im.users.online.events",
                "event", "login",
                "user_type", userType).increment();
        incrementActiveConnections();
    }
    
    /**
     * 记录用户下线
     */
    public void recordUserOffline(String userType, Duration sessionDuration) {
        meterRegistry.counter("im.users.online.events",
                "event", "logout", 
                "user_type", userType).increment();
        meterRegistry.timer("im.user.session.duration",
                "user_type", userType).record(sessionDuration);
        decrementActiveConnections();
    }
    
    /**
     * 记录会话创建
     */
    public void recordSessionCreated(String sessionType) {
        meterRegistry.counter("im.sessions.created.total",
                "session_type", sessionType).increment();
    }
    
    /**
     * 记录WebSocket连接状态
     */
    public void recordWebSocketConnection(String event, String reason) {
        meterRegistry.counter("im.websocket.connections",
                "event", event,
                "reason", reason).increment();
    }
    
    /**
     * 记录消息队列状态
     */
    public void recordQueueMessage(String operation) {
        switch (operation) {
            case "enqueue":
                totalMessagesInQueue.incrementAndGet();
                meterRegistry.counter("im.queue.operations", 
                        "operation", "enqueue").increment();
                break;
            case "dequeue":
                totalMessagesInQueue.decrementAndGet();
                meterRegistry.counter("im.queue.operations",
                        "operation", "dequeue").increment();
                break;
        }
    }
    
    /**
     * 记录文件上传指标
     */
    public void recordFileUpload(String fileType, long fileSize, Duration uploadTime) {
        meterRegistry.counter("im.files.uploaded.total",
                "file_type", fileType).increment();
        meterRegistry.summary("im.files.upload.size",
                "file_type", fileType).record(fileSize);
        meterRegistry.timer("im.files.upload.time",
                "file_type", fileType).record(uploadTime);
    }
    
    /**
     * 记录搜索操作
     */
    public void recordSearch(String searchType, int resultCount, Duration searchTime) {
        meterRegistry.counter("im.search.operations",
                "search_type", searchType).increment();
        meterRegistry.summary("im.search.results.count",
                "search_type", searchType).record(resultCount);
        meterRegistry.timer("im.search.time",
                "search_type", searchType).record(searchTime);
    }
    
    /**
     * 记录系统错误
     */
    public void recordSystemError(String errorType, String component) {
        meterRegistry.counter("im.system.errors",
                "error_type", errorType,
                "component", component).increment();
    }
    
    // ==================== 私有辅助方法 ====================
    
    private Counter getMessageTypeCounter(String operation, String messageType) {
        String key = operation + ":" + messageType;
        return messageTypeCounters.computeIfAbsent(key, k -> 
            Counter.builder("im.messages." + operation + ".by.type")
                .description("Messages " + operation + " by type")
                .tag("message_type", messageType)
                .register(meterRegistry));
    }
    
    private Timer getMessageTypeLatency(String messageType) {
        return messageTypeLatencies.computeIfAbsent(messageType, type ->
            Timer.builder("im.message.latency.by.type")
                .description("Message latency by type")
                .tag("message_type", type)
                .register(meterRegistry));
    }
    
    private void incrementActiveConnections() {
        totalActiveConnections.incrementAndGet();
    }
    
    private void decrementActiveConnections() {
        long current = totalActiveConnections.get();
        if (current > 0) {
            totalActiveConnections.decrementAndGet();
        }
    }
    
    /**
     * 获取当前活跃连接数
     */
    public long getActiveConnections() {
        return totalActiveConnections.get();
    }
    
    /**
     * 获取当前队列中消息数量
     */
    public long getQueueSize() {
        return totalMessagesInQueue.get();
    }
    
    /**
     * 记录自定义业务指标
     */
    public void recordCustomMetric(String metricName, String operation, String... tags) {
        meterRegistry.counter(metricName, tags).increment();
    }
    
    /**
     * 记录带值的自定义指标
     */
    public void recordCustomGauge(String metricName, double value, String... tags) {
        meterRegistry.gauge(metricName, value);
    }
}