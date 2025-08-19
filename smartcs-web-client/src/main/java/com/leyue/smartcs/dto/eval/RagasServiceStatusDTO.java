package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RAGAS服务状态DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagasServiceStatusDTO {
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务状态：RUNNING, STOPPED, ERROR, UNKNOWN
     */
    private String status;
    
    /**
     * 服务版本
     */
    private String version;
    
    /**
     * 服务地址
     */
    private String serviceUrl;
    
    /**
     * 健康检查状态
     */
    private String healthStatus;
    
    /**
     * 最后健康检查时间
     */
    private LocalDateTime lastHealthCheck;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
    
    /**
     * 可用性百分比
     */
    private Double availabilityPercentage;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 服务配置信息
     */
    private Map<String, Object> serviceConfig;
    
    /**
     * 资源使用情况
     */
    private ResourceUsage resourceUsage;
    
    /**
     * 性能指标
     */
    private PerformanceMetrics performanceMetrics;
    
    /**
     * 资源使用情况
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResourceUsage {
        
        /**
         * CPU使用率
         */
        private Double cpuUsage;
        
        /**
         * 内存使用率
         */
        private Double memoryUsage;
        
        /**
         * 磁盘使用率
         */
        private Double diskUsage;
        
        /**
         * 网络带宽使用率
         */
        private Double networkUsage;
        
        /**
         * 活跃连接数
         */
        private Integer activeConnections;
        
        /**
         * 最大连接数
         */
        private Integer maxConnections;
    }
    
    /**
     * 性能指标
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceMetrics {
        
        /**
         * 平均响应时间
         */
        private Double averageResponseTime;
        
        /**
         * 最大响应时间
         */
        private Double maxResponseTime;
        
        /**
         * 最小响应时间
         */
        private Double minResponseTime;
        
        /**
         * 请求成功率
         */
        private Double requestSuccessRate;
        
        /**
         * 每秒请求数
         */
        private Double requestsPerSecond;
        
        /**
         * 错误率
         */
        private Double errorRate;
    }
}
