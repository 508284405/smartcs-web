package com.leyue.smartcs.mcp.audit;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 工具审计统计信息
 * 
 * <p>汇总工具使用的统计数据，包括调用次数、成功率、性能指标等。
 * 用于监控、报告和性能分析。</p>
 * 
 * @author Claude
 */
@Getter
@Builder
@ToString
public class ToolAuditStats {
    
    /**
     * 总调用次数
     */
    private final long totalCalls;
    
    /**
     * 成功调用次数
     */
    private final long successfulCalls;
    
    /**
     * 失败调用次数
     */
    private final long failedCalls;
    
    /**
     * 被阻止的调用次数
     */
    private final long blockedCalls;
    
    /**
     * 成功率（百分比）
     */
    private final double successRate;
    
    /**
     * 平均执行时间（毫秒）
     */
    private final long averageExecutionTimeMs;
    
    /**
     * 最大执行时间（毫秒）
     */
    private final long maxExecutionTimeMs;
    
    /**
     * 最小执行时间（毫秒）
     */
    private final long minExecutionTimeMs;
    
    /**
     * 获取失败率（百分比）
     */
    public double getFailureRate() {
        return totalCalls > 0 ? (double) failedCalls / totalCalls * 100 : 0.0;
    }
    
    /**
     * 获取阻止率（百分比）
     */
    public double getBlockedRate() {
        return totalCalls > 0 ? (double) blockedCalls / totalCalls * 100 : 0.0;
    }
    
    /**
     * 获取平均执行时间（秒）
     */
    public double getAverageExecutionTimeSeconds() {
        return averageExecutionTimeMs / 1000.0;
    }
    
    /**
     * 获取最大执行时间（秒）
     */
    public double getMaxExecutionTimeSeconds() {
        return maxExecutionTimeMs / 1000.0;
    }
    
    /**
     * 获取最小执行时间（秒）
     */
    public double getMinExecutionTimeSeconds() {
        return minExecutionTimeMs / 1000.0;
    }
    
    /**
     * 检查系统是否健康
     * 基于成功率和性能指标判断
     */
    public boolean isHealthy() {
        // 成功率大于95%且平均响应时间小于5秒视为健康
        return successRate >= 95.0 && getAverageExecutionTimeSeconds() <= 5.0;
    }
    
    /**
     * 获取系统健康状态描述
     */
    public String getHealthStatus() {
        if (isHealthy()) {
            return "健康";
        } else if (successRate >= 90.0) {
            return "良好";
        } else if (successRate >= 80.0) {
            return "警告";
        } else {
            return "异常";
        }
    }
}