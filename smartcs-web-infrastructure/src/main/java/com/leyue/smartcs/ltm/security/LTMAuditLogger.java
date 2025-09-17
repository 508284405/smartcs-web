package com.leyue.smartcs.ltm.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * LTM审计日志器
 * 记录LTM系统的安全和访问事件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LTMAuditLogger {

    @Value("${smartcs.ai.ltm.security.access-control.audit-logging:true}")
    private boolean auditLoggingEnabled;

    @Value("${smartcs.ai.ltm.security.audit.batch-size:100}")
    private int batchSize = 100;

    @Value("${smartcs.ai.ltm.security.audit.flush-interval-seconds:30}")
    private int flushIntervalSeconds = 30;

    // 审计事件队列
    private final ConcurrentLinkedQueue<AuditEvent> auditQueue = new ConcurrentLinkedQueue<>();
    
    // 定时刷新执行器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "ltm-audit-logger")
    );

    private final ObjectProvider<LTMAuditEventSink> auditEventSinkProvider;

    private volatile boolean initialized = false;

    /**
     * 记录审计事件
     */
    public void log(Long userId, String action, String details) {
        if (!auditLoggingEnabled) {
            return;
        }

        initializeIfNeeded();

        AuditEvent event = AuditEvent.builder()
            .timestamp(System.currentTimeMillis())
            .userId(userId)
            .action(action)
            .details(details)
            .ipAddress(getCurrentUserIP())
            .sessionId(getCurrentSessionId())
            .build();

        auditQueue.offer(event);

        // 如果队列太满，触发立即刷新
        if (auditQueue.size() >= batchSize) {
            CompletableFuture.runAsync(this::flushAuditEvents);
        }
    }

    /**
     * 记录记忆访问事件
     */
    public void logMemoryAccess(Long userId, String memoryType, Long memoryId, String operation, boolean success) {
        String details = String.format("Memory access: type=%s, id=%s, operation=%s, success=%s",
                                     memoryType, memoryId, operation, success);
        log(userId, success ? "MEMORY_ACCESS_SUCCESS" : "MEMORY_ACCESS_FAILED", details);
    }

    /**
     * 记录记忆修改事件
     */
    public void logMemoryModification(Long userId, String memoryType, Long memoryId, String operation, Object oldValue, Object newValue) {
        String details = String.format("Memory modification: type=%s, id=%s, operation=%s, hasChange=%s",
                                     memoryType, memoryId, operation, !equals(oldValue, newValue));
        log(userId, "MEMORY_MODIFIED", details);
    }

    /**
     * 记录安全违规事件
     */
    public void logSecurityViolation(Long userId, String violationType, String details) {
        log(userId, "SECURITY_VIOLATION_" + violationType, details);
        
        // 安全违规事件立即刷新
        CompletableFuture.runAsync(this::flushAuditEvents);
    }

    /**
     * 记录数据导出事件
     */
    public void logDataExport(Long userId, String dataType, int recordCount) {
        String details = String.format("Data export: type=%s, records=%d", dataType, recordCount);
        log(userId, "DATA_EXPORT", details);
    }

    /**
     * 记录数据删除事件
     */
    public void logDataDeletion(Long userId, String dataType, int recordCount, String reason) {
        String details = String.format("Data deletion: type=%s, records=%d, reason=%s", 
                                     dataType, recordCount, reason);
        log(userId, "DATA_DELETION", details);
    }

    /**
     * 记录配置变更事件
     */
    public void logConfigurationChange(Long userId, String configKey, Object oldValue, Object newValue) {
        String details = String.format("Configuration change: key=%s, changed=%s", 
                                     configKey, !equals(oldValue, newValue));
        log(userId, "CONFIGURATION_CHANGED", details);
    }

    /**
     * 获取审计统计信息
     */
    public AuditStatistics getAuditStatistics(Long userId, long startTime, long endTime) {
        // 简化实现，实际应该从持久化存储查询
        return AuditStatistics.builder()
            .userId(userId)
            .startTime(startTime)
            .endTime(endTime)
            .totalEvents(0L)
            .accessEvents(0L)
            .modificationEvents(0L)
            .securityEvents(0L)
            .build();
    }

    // 私有方法

    private void initializeIfNeeded() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    // 启动定时刷新任务
                    scheduler.scheduleAtFixedRate(
                        this::flushAuditEvents,
                        flushIntervalSeconds,
                        flushIntervalSeconds,
                        TimeUnit.SECONDS
                    );
                    initialized = true;
                    log.info("LTM审计日志器已初始化");
                }
            }
        }
    }

    private void flushAuditEvents() {
        if (auditQueue.isEmpty()) {
            return;
        }

        try {
            java.util.List<AuditEvent> events = new java.util.ArrayList<>();
            
            // 批量取出事件
            for (int i = 0; i < batchSize && !auditQueue.isEmpty(); i++) {
                AuditEvent event = auditQueue.poll();
                if (event != null) {
                    events.add(event);
                }
            }

            if (!events.isEmpty()) {
                persistAuditEvents(events);
                log.debug("已刷新{}条审计事件", events.size());
            }

        } catch (Exception e) {
            log.error("刷新审计事件失败", e);
        }
    }

    private void persistAuditEvents(java.util.List<AuditEvent> events) {
        // 这里应该将审计事件持久化到数据库或其他存储系统
        // 简化实现：记录到日志，并尝试调用可选的审计事件下沉器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AuditEvent event : events) {
            String timestamp = LocalDateTime.now().format(formatter);
            log.info("AUDIT: [{}] User={} Action={} Details={} IP={} Session={}",
                    timestamp, event.getUserId(), event.getAction(),
                    event.getDetails(), event.getIpAddress(), event.getSessionId());
        }

        LTMAuditEventSink sink = auditEventSinkProvider.getIfAvailable();
        if (sink != null) {
            try {
                sink.persistBatch(List.copyOf(events));
            } catch (Exception ex) {
                log.warn("落地审计事件到外部存储失败: {}", ex.getMessage());
            }
        }
    }

    private String getCurrentUserIP() {
        // 简化实现，实际应该从HTTP请求中获取
        return "unknown";
    }

    private String getCurrentSessionId() {
        // 简化实现，实际应该从会话上下文中获取
        return "unknown";
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * 审计事件
     */
    @lombok.Data
    @lombok.Builder
    public static class AuditEvent {
        private Long timestamp;
        private Long userId;
        private String action;
        private String details;
        private String ipAddress;
        private String sessionId;
    }

    /**
     * 审计统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class AuditStatistics {
        private Long userId;
        private Long startTime;
        private Long endTime;
        private Long totalEvents;
        private Long accessEvents;
        private Long modificationEvents;
        private Long securityEvents;
    }

    /**
     * 关闭审计日志器
     */
    public void shutdown() {
        if (initialized) {
            flushAuditEvents(); // 最后刷新一次
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("LTM审计日志器已关闭");
        }
    }
}
