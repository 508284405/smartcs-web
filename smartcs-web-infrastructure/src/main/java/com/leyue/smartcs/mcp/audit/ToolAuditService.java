package com.leyue.smartcs.mcp.audit;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.leyue.smartcs.service.TracingSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 工具审计服务
 * 
 * <p>提供工具使用的审计记录、监控统计和安全分析功能。
 * 记录所有工具调用的详细信息，用于合规性检查、安全分析和性能监控。</p>
 * 
 * <h3>审计功能:</h3>
 * <ul>
 *   <li>调用记录 - 记录每次工具调用的完整信息</li>
 *   <li>结果追踪 - 跟踪工具执行结果和异常</li>
 *   <li>性能监控 - 统计调用延迟、成功率等指标</li>
 *   <li>安全分析 - 识别异常行为和安全风险</li>
 * </ul>
 * 
 * <h3>监控指标:</h3>
 * <ul>
 *   <li>调用统计 - 总调用数、成功数、失败数</li>
 *   <li>性能指标 - 平均响应时间、最大/最小延迟</li>
 *   <li>安全事件 - 被拒绝的调用、异常模式</li>
 *   <li>用户行为 - 高频用户、异常会话</li>
 * </ul>
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ToolAuditService {

    // 统计计数器
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong successfulCalls = new AtomicLong(0);
    private final AtomicLong failedCalls = new AtomicLong(0);
    private final AtomicLong blockedCalls = new AtomicLong(0);
    
    // 性能统计
    private final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
    private final AtomicLong maxExecutionTimeMs = new AtomicLong(0);
    private final AtomicLong minExecutionTimeMs = new AtomicLong(Long.MAX_VALUE);

    /**
     * 记录工具调用开始
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 审计记录ID
     */
    public String recordToolCallStart(String sessionId, Long userId, String toolName, Object parameters) {
        String auditId = generateAuditId();
        
        ToolAuditRecord record = ToolAuditRecord.builder()
                .auditId(auditId)
                .sessionId(sessionId)
                .userId(userId)
                .toolName(toolName)
                .parameters(parameters != null ? parameters.toString() : null)
                .startTime(Instant.now())
                .status(ToolAuditRecord.Status.STARTED)
                .build();

        // 异步记录审计信息
        TracingSupport.runAsync(() -> {
            try {
                persistAuditRecord(record);
                log.debug("工具调用审计记录创建: auditId={}, toolName={}", auditId, toolName);
            } catch (Exception e) {
                log.error("创建审计记录失败: auditId={}", auditId, e);
            }
        });

        // 更新统计
        totalCalls.incrementAndGet();
        
        return auditId;
    }

    /**
     * 记录工具调用成功完成
     * 
     * @param auditId 审计记录ID
     * @param result 执行结果
     * @param executionTimeMs 执行时间（毫秒）
     */
    public void recordToolCallSuccess(String auditId, Object result, long executionTimeMs) {
        TracingSupport.runAsync(() -> {
            try {
                updateAuditRecord(auditId, ToolAuditRecord.Status.SUCCESS, 
                                result != null ? result.toString() : null, 
                                null, executionTimeMs);
                
                log.debug("工具调用成功记录: auditId={}, executionTimeMs={}ms", auditId, executionTimeMs);
            } catch (Exception e) {
                log.error("更新成功审计记录失败: auditId={}", auditId, e);
            }
        });

        // 更新统计
        successfulCalls.incrementAndGet();
        updateExecutionStats(executionTimeMs);
    }

    /**
     * 记录工具调用失败
     * 
     * @param auditId 审计记录ID
     * @param error 错误信息
     * @param executionTimeMs 执行时间（毫秒）
     */
    public void recordToolCallFailure(String auditId, String error, long executionTimeMs) {
        TracingSupport.runAsync(() -> {
            try {
                updateAuditRecord(auditId, ToolAuditRecord.Status.FAILED, 
                                null, error, executionTimeMs);
                
                log.warn("工具调用失败记录: auditId={}, error={}, executionTimeMs={}ms", 
                        auditId, error, executionTimeMs);
            } catch (Exception e) {
                log.error("更新失败审计记录失败: auditId={}", auditId, e);
            }
        });

        // 更新统计
        failedCalls.incrementAndGet();
        updateExecutionStats(executionTimeMs);
    }

    /**
     * 记录工具调用被阻止
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param toolName 工具名称
     * @param reason 阻止原因
     */
    public void recordToolCallBlocked(String sessionId, Long userId, String toolName, String reason) {
        String auditId = generateAuditId();
        
        ToolAuditRecord record = ToolAuditRecord.builder()
                .auditId(auditId)
                .sessionId(sessionId)
                .userId(userId)
                .toolName(toolName)
                .startTime(Instant.now())
                .endTime(Instant.now())
                .status(ToolAuditRecord.Status.BLOCKED)
                .errorMessage(reason)
                .executionTimeMs(0L)
                .build();

        TracingSupport.runAsync(() -> {
            try {
                persistAuditRecord(record);
                log.warn("工具调用被阻止记录: auditId={}, toolName={}, reason={}", 
                        auditId, toolName, reason);
            } catch (Exception e) {
                log.error("创建阻止审计记录失败: auditId={}", auditId, e);
            }
        });

        // 更新统计
        blockedCalls.incrementAndGet();
        
        // 安全事件分析
        analyzeSecurityEvent(sessionId, userId, toolName, reason);
    }

    /**
     * 记录安全事件
     */
    private void analyzeSecurityEvent(String sessionId, Long userId, String toolName, String reason) {
        TracingSupport.runAsync(() -> {
            try {
                // 这里可以实现更复杂的安全分析逻辑
                // 例如：检测频繁的安全违规、识别可疑行为模式等
                log.info("安全事件分析: sessionId={}, userId={}, toolName={}, reason={}", 
                        sessionId, userId, toolName, reason);
                
                // 可以集成到安全告警系统
                // securityAlertService.reportSecurityEvent(...)
                
            } catch (Exception e) {
                log.error("安全事件分析失败: sessionId={}", sessionId, e);
            }
        });
    }

    /**
     * 获取审计统计信息
     */
    public ToolAuditStats getAuditStats() {
        long total = totalCalls.get();
        long successful = successfulCalls.get();
        long failed = failedCalls.get();
        long blocked = blockedCalls.get();
        
        double successRate = total > 0 ? (double) successful / total * 100 : 0.0;
        long avgExecutionTime = successful > 0 ? totalExecutionTimeMs.get() / successful : 0;
        
        return ToolAuditStats.builder()
                .totalCalls(total)
                .successfulCalls(successful)
                .failedCalls(failed)
                .blockedCalls(blocked)
                .successRate(successRate)
                .averageExecutionTimeMs(avgExecutionTime)
                .maxExecutionTimeMs(maxExecutionTimeMs.get())
                .minExecutionTimeMs(minExecutionTimeMs.get() == Long.MAX_VALUE ? 0 : minExecutionTimeMs.get())
                .build();
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        totalCalls.set(0);
        successfulCalls.set(0);
        failedCalls.set(0);
        blockedCalls.set(0);
        totalExecutionTimeMs.set(0);
        maxExecutionTimeMs.set(0);
        minExecutionTimeMs.set(Long.MAX_VALUE);
        
        log.info("审计统计信息已重置");
    }

    /**
     * 更新执行时间统计
     */
    private void updateExecutionStats(long executionTimeMs) {
        totalExecutionTimeMs.addAndGet(executionTimeMs);
        
        // 更新最大执行时间
        long currentMax = maxExecutionTimeMs.get();
        while (executionTimeMs > currentMax && 
               !maxExecutionTimeMs.compareAndSet(currentMax, executionTimeMs)) {
            currentMax = maxExecutionTimeMs.get();
        }
        
        // 更新最小执行时间
        long currentMin = minExecutionTimeMs.get();
        while (executionTimeMs < currentMin && 
               !minExecutionTimeMs.compareAndSet(currentMin, executionTimeMs)) {
            currentMin = minExecutionTimeMs.get();
        }
    }

    /**
     * 生成审计记录ID
     */
    private String generateAuditId() {
        return "audit_" + System.currentTimeMillis() + "_" + 
               Thread.currentThread().getId();
    }

    /**
     * 持久化审计记录
     */
    private void persistAuditRecord(ToolAuditRecord record) {
        // 这里可以实现具体的持久化逻辑
        // 例如：写入数据库、日志文件、或消息队列
        log.debug("持久化审计记录: {}", record);
        
        // 示例：可以写入数据库
        // auditRepository.save(record);
        
        // 示例：可以发送到消息队列进行异步处理
        // messageQueue.send(record);
    }

    /**
     * 更新审计记录
     */
    private void updateAuditRecord(String auditId, ToolAuditRecord.Status status, 
                                 String result, String error, long executionTimeMs) {
        // 这里实现具体的更新逻辑
        log.debug("更新审计记录: auditId={}, status={}, executionTimeMs={}ms", 
                 auditId, status, executionTimeMs);
        
        // 示例：更新数据库记录
        // auditRepository.updateRecord(auditId, status, result, error, executionTimeMs);
    }
}
