package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RAGAS连接测试结果DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagasConnectionTestResultDTO {
    
    /**
     * 连接状态：SUCCESS, FAILED, TIMEOUT, UNKNOWN
     */
    private String connectionStatus;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 测试时间
     */
    private LocalDateTime testTime;
    
    /**
     * 服务端点
     */
    private String serviceEndpoint;
    
    /**
     * 连接配置
     */
    private Map<String, Object> connectionConfig;
    
    /**
     * 网络延迟
     */
    private Long networkLatency;
    
    /**
     * 连接建立时间
     */
    private Long connectionEstablishTime;
    
    /**
     * 是否支持SSL
     */
    private Boolean supportsSsl;
    
    /**
     * SSL证书信息
     */
    private SslCertificateInfo sslCertificateInfo;
    
    /**
     * 连接池状态
     */
    private ConnectionPoolStatus connectionPoolStatus;
    
    /**
     * 测试详情
     */
    private Map<String, Object> testDetails;
    
    /**
     * SSL证书信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SslCertificateInfo {
        
        /**
         * 证书主题
         */
        private String subject;
        
        /**
         * 证书颁发者
         */
        private String issuer;
        
        /**
         * 证书有效期开始
         */
        private LocalDateTime validFrom;
        
        /**
         * 证书有效期结束
         */
        private LocalDateTime validTo;
        
        /**
         * 证书序列号
         */
        private String serialNumber;
        
        /**
         * 证书指纹
         */
        private String fingerprint;
        
        /**
         * 是否有效
         */
        private Boolean isValid;
    }
    
    /**
     * 连接池状态
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConnectionPoolStatus {
        
        /**
         * 活跃连接数
         */
        private Integer activeConnections;
        
        /**
         * 空闲连接数
         */
        private Integer idleConnections;
        
        /**
         * 最大连接数
         */
        private Integer maxConnections;
        
        /**
         * 连接池使用率
         */
        private Double poolUsagePercentage;
        
        /**
         * 等待连接数
         */
        private Integer waitingConnections;
    }
}
